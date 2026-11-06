package com.example.daon.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.temporal.WeekFields;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.daon.mapper.ProductMapper;
import com.example.daon.vo.EmployeeLeaveVo;
import com.example.daon.vo.EmployeeVo;
import com.example.daon.vo.EmployeePayrollVo;
import com.example.daon.vo.EmployeeWorkAllowanceVo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "hwp", "hwpx");
    private final ProductMapper productMapper;
    private final AuthService authService;
    private final Path resumeDirectory = Path.of("data", "resumes").toAbsolutePath().normalize();

    public List<EmployeeVo> getEmployees() { return productMapper.selectEmployeeList(); }
    public EmployeeVo getEmployee(Long id) {
        EmployeeVo employee = productMapper.selectEmployeeById(id);
        if (employee == null) throw new IllegalArgumentException("직원을 찾을 수 없습니다.");
        return employee;
    }
    public List<EmployeeLeaveVo> getLeaveHistory(Long id) { return productMapper.selectEmployeeLeaveList(id); }
    public List<EmployeePayrollVo> getPayrollHistory(Long id) { return productMapper.selectEmployeePayrollList(id); }
    @Transactional public void deleteLeave(Long historyId,Long employeeId,String user){getEmployee(employeeId);if(productMapper.deleteEmployeeLeave(historyId,employeeId)==0)throw new IllegalArgumentException("연차 이력을 찾을 수 없습니다.");authService.audit(user,"DELETE","EMPLOYEE_LEAVE",String.valueOf(historyId),"연차 이력 삭제");}
    @Transactional public void deletePayroll(Long payrollId,Long employeeId,String user){getEmployee(employeeId);if(productMapper.deleteEmployeePayroll(payrollId,employeeId)==0)throw new IllegalArgumentException("급여 이력을 찾을 수 없습니다.");authService.audit(user,"DELETE","EMPLOYEE_PAYROLL",String.valueOf(payrollId),"급여 이력 삭제");}
    @Transactional public void deleteWorkAllowance(Long employeeId,java.time.LocalDate date,String user){getEmployee(employeeId);if(productMapper.deleteEmployeeWorkAllowance(employeeId,date)==0)throw new IllegalArgumentException("해당 날짜 기록을 찾을 수 없습니다.");authService.audit(user,"DELETE","WORK_ALLOWANCE",String.valueOf(employeeId),date+" 작업·수당 삭제");}
    @Transactional
    public void deleteEmployee(Long id, String username) {
        EmployeeVo employee = getEmployee(id);
        if (productMapper.softDeleteEmployee(id, username) == 0) throw new IllegalArgumentException("직원을 찾을 수 없습니다.");
        authService.audit(username, "DELETE", "EMPLOYEE", String.valueOf(id), employee.getEmployeeName()+" 직원 삭제(기록 보존)");
    }
    public List<EmployeeWorkAllowanceVo> getWorkAllowances(Long id, String month) { return productMapper.selectEmployeeWorkAllowanceList(id, month); }
    public BigDecimal getWorkAllowanceTotal(Long id, String month) {
        BigDecimal total = productMapper.selectEmployeeWorkAllowanceTotal(id, month);
        return total == null ? BigDecimal.ZERO : total;
    }
    public BigDecimal getWeeklyHolidayAllowanceTotal(Long id, String month) {
        EmployeeVo employee = getEmployee(id);
        if (valueOrZero(employee.getWeeklyScheduledHours()).compareTo(new BigDecimal("15")) < 0) return BigDecimal.ZERO;
        int requiredDays = employee.getWeeklyScheduledDays() == null ? 5 : employee.getWeeklyScheduledDays();
        Map<String, Long> attendedByWeek = getWorkAllowances(id, month).stream()
                .filter(w -> Boolean.TRUE.equals(w.getAttended()))
                .collect(Collectors.groupingBy(w -> w.getWorkDate().getYear()+"-"+w.getWorkDate().get(WeekFields.ISO.weekOfWeekBasedYear()), Collectors.counting()));
        long eligibleWeeks = attendedByWeek.values().stream().filter(days -> days >= requiredDays).count();
        return valueOrZero(employee.getHourlyWage()).multiply(valueOrZero(employee.getDailyScheduledHours()))
                .multiply(BigDecimal.valueOf(eligibleWeeks)).setScale(0, RoundingMode.HALF_UP);
    }

    @Transactional
    public void saveWorkAllowance(EmployeeWorkAllowanceVo work, String username) {
        EmployeeVo employee = getEmployee(work.getEmployeeId());
        if (work.getWorkDate() == null) throw new IllegalArgumentException("작업 날짜를 선택하세요.");
        int quantity = work.getBoardQuantity() == null ? 0 : work.getBoardQuantity();
        BigDecimal unitPrice = valueOrZero(work.getBoardUnitPrice());
        BigDecimal hours = valueOrZero(work.getOvertimeHours());
        BigDecimal hourlyWage = valueOrZero(employee.getHourlyWage());
        if (quantity < 0 || unitPrice.signum() < 0 || hours.signum() < 0 || hourlyWage.signum() < 0)
            throw new IllegalArgumentException("수량, 단가, 시간과 시급은 0 이상이어야 합니다.");
        BigDecimal boardPay = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal overtimePay = hourlyWage.multiply(hours).multiply(new BigDecimal("1.5")).setScale(0, RoundingMode.HALF_UP);
        work.setBoardQuantity(quantity); work.setBoardUnitPrice(unitPrice); work.setBoardAllowance(boardPay);
        work.setOvertimeHours(hours); work.setHourlyWage(hourlyWage); work.setOvertimeMultiplier(new BigDecimal("1.5"));
        work.setOvertimeAllowance(overtimePay); work.setTotalAllowance(boardPay.add(overtimePay));
        if (work.getAttended() == null) work.setAttended(true);
        work.setCreatedBy(username); work.setUpdatedBy(username);
        productMapper.upsertEmployeeWorkAllowance(work);
        authService.audit(username, "WORK_ALLOWANCE_SAVE", "EMPLOYEE", String.valueOf(employee.getEmployeeId()),
                employee.getEmployeeName()+" / "+work.getWorkDate()+" / 도마 "+quantity+"개 / 야근 "+hours+"시간");
    }

    @Transactional
    public void saveEmployee(EmployeeVo employee, MultipartFile resume, String username) {
        normalize(employee);
        storeResume(employee, resume);
        employee.setUpdatedBy(username);
        if (employee.getEmployeeId() == null) {
            employee.setCreatedBy(username);
            productMapper.insertEmployee(employee);
            authService.audit(username, "CREATE", "EMPLOYEE", String.valueOf(employee.getEmployeeId()), employee.getEmployeeName());
        } else {
            EmployeeVo before = getEmployee(employee.getEmployeeId());
            if (productMapper.updateEmployee(employee) == 0) throw new IllegalArgumentException("직원을 찾을 수 없습니다.");
            authService.audit(username, "UPDATE", "EMPLOYEE", String.valueOf(employee.getEmployeeId()),
                    before.getEmployeeName() + " → " + employee.getEmployeeName());
        }
    }

    @Transactional
    public void addLeave(EmployeeLeaveVo leave, String username) {
        EmployeeVo employee = getEmployee(leave.getEmployeeId());
        if (leave.getDays() == null || leave.getDays().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("연차 일수는 0보다 커야 합니다.");
        if (leave.getLeaveDate() == null) throw new IllegalArgumentException("연차 사용일을 입력하세요.");
        if (employee.getAnnualLeaveRemaining().compareTo(leave.getDays()) < 0)
            throw new IllegalArgumentException("남은 연차보다 많이 사용할 수 없습니다.");
        leave.setLeaveType("ANNUAL");
        leave.setCreatedBy(username);
        productMapper.insertEmployeeLeave(leave);
        authService.audit(username, "LEAVE_USE", "EMPLOYEE", String.valueOf(employee.getEmployeeId()),
                employee.getEmployeeName() + " / " + leave.getDays() + "일 / " + leave.getLeaveDate());
    }

    public Resource getResume(EmployeeVo employee) {
        if (employee.getResumeStoredName() == null) throw new IllegalArgumentException("등록된 이력서가 없습니다.");
        try {
            Path path = resumeDirectory.resolve(employee.getResumeStoredName()).normalize();
            if (!path.startsWith(resumeDirectory)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) throw new IllegalArgumentException("이력서 파일을 찾을 수 없습니다.");
            return resource;
        } catch (IOException e) { throw new IllegalStateException("이력서를 열 수 없습니다.", e); }
    }

    @Transactional
    public void savePayroll(EmployeePayrollVo payroll, String username) {
        EmployeeVo employee = getEmployee(payroll.getEmployeeId());
        if (payroll.getPayMonth() == null || !payroll.getPayMonth().matches("\\d{4}-(0[1-9]|1[0-2])"))
            throw new IllegalArgumentException("급여월을 올바르게 입력하세요.");
        BigDecimal base = payroll.getBaseSalary() == null ? BigDecimal.ZERO : payroll.getBaseSalary();
        BigDecimal extraAllowance = payroll.getAllowance() == null ? BigDecimal.ZERO : payroll.getAllowance();
        BigDecimal allowance = extraAllowance.add(getWorkAllowanceTotal(payroll.getEmployeeId(), payroll.getPayMonth()))
                .add(getWeeklyHolidayAllowanceTotal(payroll.getEmployeeId(), payroll.getPayMonth()));
        BigDecimal deduction = payroll.getDeduction() == null ? BigDecimal.ZERO : payroll.getDeduction();
        if (base.signum() < 0 || allowance.signum() < 0 || deduction.signum() < 0)
            throw new IllegalArgumentException("급여 금액은 0원 이상이어야 합니다.");
        BigDecimal net = base.add(allowance).subtract(deduction);
        if (net.signum() < 0) throw new IllegalArgumentException("공제액이 지급액보다 클 수 없습니다.");
        payroll.setBaseSalary(base); payroll.setExtraAllowance(extraAllowance); payroll.setAllowance(allowance); payroll.setDeduction(deduction); payroll.setNetSalary(net);
        if (!Set.of("PENDING", "PAID").contains(payroll.getPaymentStatus())) payroll.setPaymentStatus("PENDING");
        payroll.setCreatedBy(username); payroll.setUpdatedBy(username);
        productMapper.upsertEmployeePayroll(payroll);
        authService.audit(username, "PAYROLL_SAVE", "EMPLOYEE", String.valueOf(employee.getEmployeeId()),
                employee.getEmployeeName() + " / " + payroll.getPayMonth() + " / 실지급 " + net.toPlainString() + "원");
    }

    private BigDecimal valueOrZero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private void normalize(EmployeeVo employee) {
        if (employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank()) throw new IllegalArgumentException("사번을 입력하세요.");
        if (employee.getEmployeeName() == null || employee.getEmployeeName().isBlank()) throw new IllegalArgumentException("직원명을 입력하세요.");
        if (employee.getAnnualLeaveTotal() == null) employee.setAnnualLeaveTotal(new BigDecimal("15.0"));
        if (employee.getAnnualLeaveTotal().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("총 연차는 0 이상이어야 합니다.");
        if (employee.getHourlyWage() == null) employee.setHourlyWage(BigDecimal.ZERO);
        if (employee.getWeeklyScheduledHours() == null) employee.setWeeklyScheduledHours(new BigDecimal("40"));
        if (employee.getWeeklyScheduledDays() == null) employee.setWeeklyScheduledDays(5);
        if (employee.getDailyScheduledHours() == null) employee.setDailyScheduledHours(new BigDecimal("8"));
        if (employee.getHourlyWage().signum()<0 || employee.getWeeklyScheduledHours().signum()<0 || employee.getWeeklyScheduledDays()<1 || employee.getDailyScheduledHours().signum()<0)
            throw new IllegalArgumentException("시급과 근로시간을 올바르게 입력하세요.");
        if (employee.getEmploymentStatus() == null || employee.getEmploymentStatus().isBlank()) employee.setEmploymentStatus("ACTIVE");
    }

    private void storeResume(EmployeeVo employee, MultipartFile resume) {
        if (resume == null || resume.isEmpty()) return;
        if (resume.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("이력서는 10MB 이하만 등록할 수 있습니다.");
        String original = Path.of(resume.getOriginalFilename() == null ? "resume" : resume.getOriginalFilename()).getFileName().toString();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) throw new IllegalArgumentException("이력서는 PDF, DOC, DOCX, HWP, HWPX 파일만 등록할 수 있습니다.");
        String stored = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(resumeDirectory);
            Files.copy(resume.getInputStream(), resumeDirectory.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new IllegalStateException("이력서 저장에 실패했습니다.", e); }
        employee.setResumeOriginalName(original);
        employee.setResumeStoredName(stored);
    }
}
