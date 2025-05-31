package com.example.SanChoi247.controller;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.SanChoi247.model.entity.User;
import com.example.SanChoi247.model.repo.LoginRepo;
import com.example.SanChoi247.model.repo.UserRepo;
import com.example.SanChoi247.service.EmailService;
import com.example.SanChoi247.service.FileUploadService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    @Autowired
    private PasswordEncoder passwordEncoder; // Tiêm PasswordEncoder
    @Autowired
    UserRepo userRepo;
    @Autowired
    LoginRepo loginRepo;
    @Autowired
    EmailService emailService;
    @Autowired
    FileUploadService fileUpload;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //------------------------------
    @GetMapping("/ShowChangePassword")
    public String showChangePasswordForm() {
        return "user/changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model) {
        try {
            User user = (User) session.getAttribute("UserAfterLogin");

            // Kiểm tra mật khẩu cũ với repository
            if (!loginRepo.checkOldPassword(user.getUid(), oldPassword)) {
                model.addAttribute("error", "Old password is incorrect.");
            } else if (newPassword.equals(oldPassword)) {
                model.addAttribute("error", "New password cannot be the same as the old password.");
            } else if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "New password and confirm password do not match.");
            } else {
                // Mã hóa mật khẩu mới trước khi cập nhật vào cơ sở dữ liệu
                String encodedNewPassword = passwordEncoder.encode(newPassword);
                userRepo.updatePassword(user.getUid(), encodedNewPassword);
                model.addAttribute("message", "Password updated successfully!");
                return "redirect:/Logout"; // Chuyển hướng sau khi thay đổi thành công
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error updating password.");
            e.printStackTrace();
        }
        return "user/changePassword";
    }
    //--------------------------------
}