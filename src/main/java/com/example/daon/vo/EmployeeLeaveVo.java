package com.example.daon.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EmployeeLeaveVo {
    private Long leaveHistoryId;
    private Long employeeId;
    private LocalDate leaveDate;
    private BigDecimal days;
    private String leaveType;
    private String memo;
    private String createdBy;
    private LocalDateTime createdAt;
}
