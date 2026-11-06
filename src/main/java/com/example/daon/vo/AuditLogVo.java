package com.example.daon.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuditLogVo {
    private Long auditId;
    private String username;
    private String actionType;
    private String targetType;
    private String targetId;
    private String details;
    private LocalDateTime createdAt;
}
