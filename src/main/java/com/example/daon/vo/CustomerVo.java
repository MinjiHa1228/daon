package com.example.daon.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CustomerVo {
    private Long customerId;
    private String customerName;
    private String managerName;
    private String phone;
    private String email;
    private String address;
    private String memo;
    private String useYn;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
