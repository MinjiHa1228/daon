package com.example.daon.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EmployeePayrollVo {
    private Long payrollId;
    private Long employeeId;
    private String payMonth;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private BigDecimal extraAllowance;
    private BigDecimal deduction;
    private BigDecimal netSalary;
    private String paymentStatus;
    private LocalDate paymentDate;
    private String memo;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
