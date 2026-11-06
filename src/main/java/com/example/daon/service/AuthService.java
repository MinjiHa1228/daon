package com.example.daon.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.daon.mapper.ProductMapper;
import com.example.daon.vo.AuditLogVo;
import com.example.daon.vo.UserVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ProductMapper productMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean needsSetup() {
        return productMapper.countUsers() == 0;
    }

    public UserVo authenticate(String username, String password) {
        UserVo user = productMapper.selectUserByUsername(username);
        if (user == null || !"Y".equals(user.getUseYn()) || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    public void createUser(String username, String password, String displayName, String role, String actor) {
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,50}")) {
            throw new IllegalArgumentException("아이디는 영문, 숫자, . _ - 조합으로 3자 이상 입력하세요.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (!List.of("ADMIN", "EMPLOYEE").contains(role)) {
            throw new IllegalArgumentException("올바르지 않은 권한입니다.");
        }
        if (productMapper.selectUserByUsername(username) != null) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        UserVo user = new UserVo();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        user.setRole(role);
        productMapper.insertUser(user);
        productMapper.insertAudit(actor == null ? username : actor, "CREATE", "USER", username,
                displayName + " / " + role);
    }

    public List<UserVo> getUsers() { return productMapper.selectUserList(); }
    public void updateUser(String username,String displayName,String role,String password,String actor){
        UserVo before=productMapper.selectUserByUsername(username);if(before==null||!"Y".equals(before.getUseYn()))throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        if(!List.of("ADMIN","EMPLOYEE").contains(role))throw new IllegalArgumentException("권한을 확인하세요.");
        if("ADMIN".equals(before.getRole())&&!"ADMIN".equals(role)&&productMapper.countActiveAdmins()<=1)throw new IllegalArgumentException("마지막 관리자 권한은 변경할 수 없습니다.");
        String hash=null;if(password!=null&&!password.isBlank()){if(password.length()<8)throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");hash=passwordEncoder.encode(password);}
        if(productMapper.updateUser(username,displayName,role,hash)==0)throw new IllegalArgumentException("사용자를 수정할 수 없습니다.");audit(actor,"UPDATE","USER",username,displayName+" / "+role);
    }
    public void deleteUser(String username,String actor){UserVo u=productMapper.selectUserByUsername(username);if(u==null)throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");if(username.equals(actor))throw new IllegalArgumentException("현재 로그인한 본인 계정은 삭제할 수 없습니다.");if("ADMIN".equals(u.getRole())&&productMapper.countActiveAdmins()<=1)throw new IllegalArgumentException("마지막 관리자 계정은 삭제할 수 없습니다.");if(productMapper.softDeleteUser(username)==0)throw new IllegalArgumentException("사용자를 삭제할 수 없습니다.");audit(actor,"DELETE","USER",username,u.getDisplayName()+" 계정 중지");}
    public List<AuditLogVo> getAuditLogs(int page, int size) {
        productMapper.deleteExpiredAuditLogs();
        return productMapper.selectAuditList(Math.max(0, page) * size, size);
    }
    public int getAuditLogCount() {
        productMapper.deleteExpiredAuditLogs();
        return productMapper.countAuditLogs();
    }
    public void audit(String user, String action, String targetType, String targetId, String details) {
        productMapper.deleteExpiredAuditLogs();
        productMapper.insertAudit(user, action, targetType, targetId, details);
    }
}
