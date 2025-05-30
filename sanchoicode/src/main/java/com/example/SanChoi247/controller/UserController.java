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
    @GetMapping("/ShowEditProfile")
    public String showEditProfile(HttpSession httpSession, Model model) throws Exception {
        User user = (User) httpSession.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login"; // Chuyển hướng về trang đăng nhập nếu chưa đăng nhập
        }
        User newU = userRepo.getUserById(user.getUid());
        model.addAttribute("user", newU);
        return "user/editProfile"; // Trả về trang chỉnh sửa hồ sơ
    }
    //------------------------------
     @PostMapping(value = "/EditProfile")
    public String editUser(
            @RequestParam("name") String nameInput,
            @RequestParam("phone") String phoneInput,
            @RequestParam("email") String emailInput,
            @RequestParam("dob") Date dobInput,
            @RequestParam("gender") char genderInput,
            @RequestParam(value = "ten_san", required = false) String ten_sanInput,
            @RequestParam(value = "address", required = false) String addressInput,
            @RequestParam(value = "img_san1", required = false) String img_san1Input,
            @RequestParam(value = "img_san2", required = false) String img_san2Input,
            @RequestParam(value = "img_san3", required = false) String img_san3Input,
            @RequestParam(value = "img_san4", required = false) String img_san4Input,
            @RequestParam(value = "img_san5", required = false) String img_san5Input,
            Model model,
            HttpSession httpSession) {
    
        User activeUser = (User) httpSession.getAttribute("UserAfterLogin");
    
        // Perform field validations
        boolean hasError = false;
        if (!isValidName(nameInput)) {
            model.addAttribute("nameError", "Invalid name format.");
            hasError = true;
        }
        if (!isValidPhone(phoneInput)) {
            model.addAttribute("phoneError", "Invalid phone number format.");
            hasError = true;
        }
        if (!isValidEmail(emailInput)) {
            model.addAttribute("emailError", "Invalid email format.");
            hasError = true;
        }
        if (dobInput.after(new Date(System.currentTimeMillis()))) {
            model.addAttribute("dobError", "Date of birth cannot be in the future.");
            hasError = true;
        }
    
        // Return to edit profile page with errors if validation failed
        if (hasError) {
            return "user/editProfile";
        }
    
        // Proceed with saving if validations pass
        try {
            userRepo.editProfile(nameInput, dobInput, genderInput, phoneInput, emailInput, activeUser.getUid());
            activeUser.setName(nameInput);
            activeUser.setPhone(phoneInput);
            activeUser.setEmail(emailInput);
            activeUser.setDob(dobInput);
            activeUser.setGender(genderInput);
    
            if (activeUser.getRole() == 'C') {
                User user = userRepo.getUserById(activeUser.getUid());
                user.setTen_san(ten_sanInput);
                user.setAddress(addressInput);
                user.setImg_san1(img_san1Input);
                user.setImg_san2(img_san2Input);
                user.setImg_san3(img_san3Input);
                user.setImg_san4(img_san4Input);
                user.setImg_san5(img_san5Input);
                userRepo.save(user);
                httpSession.setAttribute("activeOwner", user);
            }
    
            // Update session and add success message
            activeUser = userRepo.getUserById(activeUser.getUid());
            httpSession.setAttribute("UserAfterLogin", activeUser);
            model.addAttribute("message", "Edited successfully!");
    
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while editing your profile.");
        }
    
        return "user/editProfile";
    }
    //------------------------------
}