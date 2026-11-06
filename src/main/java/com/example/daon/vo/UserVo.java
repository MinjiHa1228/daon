package com.example.daon.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String passwordHash;
    private String displayName;
    private String role;
    private String useYn;
    private LocalDateTime createdAt;
}
