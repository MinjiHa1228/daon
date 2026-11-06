package com.example.daon.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EmployeeVo {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String department;
    private String positionName;
    private String phone;
    private String email;
    private LocalDate hireDate;
    private String employmentStatus;
    private BigDecimal annualLeaveTotal;
    private BigDecimal annualLeaveRecommended;
    private BigDecimal annualLeaveUsed;
    private BigDecimal annualLeaveRemaining;
    private BigDecimal hourlyWage;
    private BigDecimal weeklyScheduledHours;
    private Integer weeklyScheduledDays;
    private BigDecimal dailyScheduledHours;
    private String resumeOriginalName;
    private String resumeStoredName;
    private String memo;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
