package com.example.daon.controller;

import java.util.stream.IntStream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.daon.service.AuthService;
import com.example.daon.vo.UserVo;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/setup")
    public String setup() {
        return authService.needsSetup() ? "setup" : "redirect:/login";
    }

    @PostMapping("/setup")
    public String setupAdmin(@RequestParam String username, @RequestParam String password,
            @RequestParam String displayName, RedirectAttributes redirect) {
        if (!authService.needsSetup()) return "redirect:/login";
        try {
            authService.createUser(username, password, displayName, "ADMIN", null);
            redirect.addFlashAttribute("message", "관리자 계정이 생성되었습니다. 로그인하세요.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/setup";
        }
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        return session.getAttribute("loginUser") == null ? "login" : "redirect:/product/list";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
            HttpSession session, RedirectAttributes redirect) {
        UserVo user = authService.authenticate(username, password);
        if (user == null) {
            redirect.addFlashAttribute("error", "아이디 또는 비밀번호를 확인하세요.");
            return "redirect:/login";
        }
        user.setPasswordHash(null);
        session.setAttribute("loginUser", user);
        return "redirect:/product/list";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", authService.getUsers());
        return "admin_users";
    }

    @PostMapping("/admin/users")
    public String createUser(@RequestParam String username, @RequestParam String password,
            @RequestParam String displayName, @RequestParam String role,
            HttpSession session, RedirectAttributes redirect) {
        UserVo actor = (UserVo) session.getAttribute("loginUser");
        try {
            authService.createUser(username, password, displayName, role, actor.getUsername());
            redirect.addFlashAttribute("message", "사용자 계정이 생성되었습니다.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @PostMapping("/admin/users/update") public String updateUser(@RequestParam String username,@RequestParam String displayName,@RequestParam String role,@RequestParam(required=false) String password,HttpSession session,RedirectAttributes redirect){try{authService.updateUser(username,displayName,role,password,((UserVo)session.getAttribute("loginUser")).getUsername());redirect.addFlashAttribute("message","사용자 정보가 수정되었습니다.");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/admin/users";}
    @PostMapping("/admin/users/delete") public String deleteUser(@RequestParam String username,HttpSession session,RedirectAttributes redirect){try{authService.deleteUser(username,((UserVo)session.getAttribute("loginUser")).getUsername());redirect.addFlashAttribute("message","사용자 계정이 삭제(중지)되었습니다.");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/admin/users";}

    @GetMapping("/admin/audit")
    public String audit(@RequestParam(defaultValue = "1") int page, Model model) {
        int size = 20;
        int totalCount = authService.getAuditLogCount();
        int totalPages = Math.max(1, (totalCount + size - 1) / size);
        int currentPage = Math.max(1, Math.min(page, totalPages));
        model.addAttribute("logs", authService.getAuditLogs(currentPage - 1, size));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("hasPrevious", currentPage > 1);
        model.addAttribute("hasNext", currentPage < totalPages);
        int startPage = Math.max(1, currentPage - 4);
        int endPage = Math.min(totalPages, startPage + 9);
        startPage = Math.max(1, endPage - 9);
        model.addAttribute("pageNumbers", IntStream.rangeClosed(startPage, endPage).boxed().toList());
        return "admin_audit";
    }
}
