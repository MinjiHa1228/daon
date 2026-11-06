package com.example.daon.vo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
@Data
public class EmployeeWorkAllowanceVo {
    private Long workAllowanceId;
    private Long employeeId;
    private LocalDate workDate;
    private Integer boardQuantity;
    private BigDecimal boardUnitPrice;
    private BigDecimal boardAllowance;
    private BigDecimal overtimeHours;
    private BigDecimal hourlyWage;
    private BigDecimal overtimeMultiplier;
    private BigDecimal overtimeAllowance;
    private BigDecimal totalAllowance;
    private Boolean attended;
    private String memo;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
