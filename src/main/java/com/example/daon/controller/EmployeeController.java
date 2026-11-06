package com.example.daon.controller;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.daon.service.EmployeeService;
import com.example.daon.vo.EmployeeLeaveVo;
import com.example.daon.vo.EmployeeVo;
import com.example.daon.vo.UserVo;
import com.example.daon.vo.EmployeePayrollVo;
import com.example.daon.vo.EmployeeWorkAllowanceVo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping("/employee/list")
    public String list(@RequestParam(required=false) Long editId, @RequestParam(required=false) String workMonth, Model model) {
        var employees = employeeService.getEmployees();
        model.addAttribute("employees", employees);
        model.addAttribute("activeCount", employees.stream().filter(e -> "ACTIVE".equals(e.getEmploymentStatus())).count());
        model.addAttribute("leaveCount", employees.stream().filter(e -> "LEAVE".equals(e.getEmploymentStatus())).count());
        model.addAttribute("retiredCount", employees.stream().filter(e -> "RETIRED".equals(e.getEmploymentStatus())).count());
        if (editId != null) {
            if (workMonth == null || !workMonth.matches("\\d{4}-(0[1-9]|1[0-2])")) workMonth = YearMonth.now().toString();
            model.addAttribute("editEmployee", employeeService.getEmployee(editId));
            model.addAttribute("leaveHistory", employeeService.getLeaveHistory(editId));
            model.addAttribute("payrollHistory", employeeService.getPayrollHistory(editId));
            model.addAttribute("workMonth", workMonth);
            model.addAttribute("workAllowances", employeeService.getWorkAllowances(editId, workMonth));
            var workTotal = employeeService.getWorkAllowanceTotal(editId, workMonth);
            var weeklyHolidayTotal = employeeService.getWeeklyHolidayAllowanceTotal(editId, workMonth);
            model.addAttribute("workAllowanceTotal", workTotal.add(weeklyHolidayTotal));
            model.addAttribute("productionOvertimeTotal", workTotal);
            model.addAttribute("weeklyHolidayAllowanceTotal", weeklyHolidayTotal);
        }
        return "employee_list";
    }

    @PostMapping("/employee/work-allowance")
    public String saveWorkAllowance(EmployeeWorkAllowanceVo work, HttpSession session, RedirectAttributes redirect) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            employeeService.saveWorkAllowance(work, user.getUsername());
            redirect.addFlashAttribute("message", "날짜별 도마 작업·야근 수당이 저장되었습니다.");
        } catch (RuntimeException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        String month = work.getWorkDate() == null ? YearMonth.now().toString() : YearMonth.from(work.getWorkDate()).toString();
        return "redirect:/employee/list?editId=" + work.getEmployeeId() + "&workMonth=" + month + "&view=allowance";
    }

    @PostMapping("/employee/save")
    public String save(EmployeeVo employee, @RequestParam(required=false) MultipartFile resume,
            HttpSession session, RedirectAttributes redirect) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            employeeService.saveEmployee(employee, resume, user.getUsername());
            redirect.addFlashAttribute("message", "직원 정보가 저장되었습니다.");
        } catch (RuntimeException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/employee/list";
    }

    @PostMapping("/employee/delete")
    public String delete(@RequestParam Long employeeId, HttpSession session, RedirectAttributes redirect) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            employeeService.deleteEmployee(employeeId, user.getUsername());
            redirect.addFlashAttribute("message", "직원이 목록에서 삭제되었습니다. 기존 연차·급여 이력은 보존됩니다.");
        } catch (RuntimeException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/employee/list";
    }

    @PostMapping("/employee/leave")
    public String addLeave(EmployeeLeaveVo leave, HttpSession session, RedirectAttributes redirect) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            employeeService.addLeave(leave, user.getUsername());
            redirect.addFlashAttribute("message", "연차 사용 이력이 등록되었습니다.");
        } catch (RuntimeException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/employee/list?editId=" + leave.getEmployeeId();
    }

    @GetMapping("/employee/resume")
    public ResponseEntity<Resource> resume(@RequestParam Long employeeId) {
        EmployeeVo employee = employeeService.getEmployee(employeeId);
        Resource resource = employeeService.getResume(employee);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(employee.getResumeOriginalName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(resource);
    }

    @PostMapping("/employee/payroll")
    public String savePayroll(EmployeePayrollVo payroll, HttpSession session, RedirectAttributes redirect) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            employeeService.savePayroll(payroll, user.getUsername());
            redirect.addFlashAttribute("message", "급여 내역이 저장되었습니다.");
        } catch (RuntimeException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/employee/list?editId=" + payroll.getEmployeeId();
    }
    @PostMapping("/employee/leave/delete") public String deleteLeave(@RequestParam Long leaveHistoryId,@RequestParam Long employeeId,HttpSession s,RedirectAttributes r){try{employeeService.deleteLeave(leaveHistoryId,employeeId,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","연차 이력이 삭제되었습니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/employee/list?editId="+employeeId;}
    @PostMapping("/employee/payroll/delete") public String deletePayroll(@RequestParam Long payrollId,@RequestParam Long employeeId,HttpSession s,RedirectAttributes r){try{employeeService.deletePayroll(payrollId,employeeId,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","급여 이력이 삭제되었습니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/employee/list?editId="+employeeId;}
    @PostMapping("/employee/work-allowance/delete") public String deleteWork(@RequestParam Long employeeId,@RequestParam java.time.LocalDate workDate,HttpSession s,RedirectAttributes r){try{employeeService.deleteWorkAllowance(employeeId,workDate,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","날짜별 작업·수당 기록이 삭제되었습니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/employee/list?editId="+employeeId+"&workMonth="+java.time.YearMonth.from(workDate);}
}
