package com.example.daon.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.daon.service.AuthService;
import com.example.daon.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (authService.needsSetup()) {
            if (path.equals("/setup") || path.equals("/error")) return true;
            response.sendRedirect("/setup"); return false;
        }
        if (path.equals("/login") || path.equals("/error")) return true;
        UserVo user = (UserVo) request.getSession().getAttribute("loginUser");
        if (user == null) { response.sendRedirect("/login"); return false; }
        boolean adminOnly = path.startsWith("/admin/") || path.startsWith("/product/insert")
                || path.startsWith("/customer/") || path.startsWith("/employee/")
                || path.startsWith("/order/product/")
                || path.startsWith("/stock/adjust");
        if (adminOnly && !"ADMIN".equals(user.getRole())) {
            response.sendRedirect("/product/list?denied=1"); return false;
        }
        return true;
    }
}
