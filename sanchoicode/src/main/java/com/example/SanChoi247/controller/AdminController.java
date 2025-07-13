package com.example.SanChoi247.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.SanChoi247.config.VNPayConfig;
import com.example.SanChoi247.model.dto.AdminStatistics;
import com.example.SanChoi247.model.dto.TableAdminStatistics;
import com.example.SanChoi247.model.entity.Booking;
import com.example.SanChoi247.model.entity.Booking.PaymentStatus;
import com.example.SanChoi247.model.entity.User;
import com.example.SanChoi247.model.repo.AdminRepo;
import com.example.SanChoi247.model.repo.Baseconnection;
import com.example.SanChoi247.model.repo.BookingRepo;
import com.example.SanChoi247.model.repo.OwnerRepo;
import com.example.SanChoi247.model.repo.UserRepo;
import com.example.SanChoi247.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
@Controller
public class AdminController {
    @Autowired
    AdminRepo adminRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    OwnerRepo ownerRepo;
    @Autowired
    EmailService emailService;
    @Autowired
    VNPayConfig VNPayConfig;
    @Autowired
    BookingRepo bookingRepo;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @GetMapping("admin/pendingRequests")
    public ResponseEntity<List<User>> getPendingRequests() {
        try {
            List<User> pendingUsers = userRepo.getUsersByStatus(1); // Trạng thái 1 là "Đang chờ xét duyệt"
            return ResponseEntity.ok(pendingUsers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/admin/approveField")
        public String showApproveFieldPage(Model model) throws Exception {
            List<User> pendingFields = userRepo.findUsersByStatus(3); // Get fields with status 3 (pending approval)
            model.addAttribute("pendingFields", pendingFields);
            return "admin/approveField";
        }
    
    @PostMapping("/admin/approveField")
    public String approveField(@RequestParam("uid") int uid, @RequestParam("approve") boolean approve) throws Exception {
        User user = userRepo.getUserById(uid); // Lấy thông tin chi tiết của người dùng để lấy email của họ
if (approve) {
            userRepo.updateFieldStatus(uid, 4); // Phê duyệt trường bằng cách đặt trạng thái thành 4
            // Gửi email chấp thuận
            String subject = "Congratulations! Your Field has been Approved on SanChoi247";
            String body = "Dear " + user.getName() + ",\n\n" +
                    "We are pleased to inform you that your field submission, \"" + user.getTen_san() + "\", located at \"" + user.getAddress() + "\", " +
                    "has been successfully approved by the SanChoi247 team.\n\n" +
                    "This means that your field is now officially listed on our platform, and players can start booking it! " +
                    "We believe your field will provide an excellent space for sports enthusiasts in the community to play and enjoy their favorite games.\n\n" +
                    "Here’s what you can do next:\n" +
                    "- Visit your field listing to see how it appears to potential players.\n" +
                    "- Ensure all details, including images and location, are up-to-date.\n" +
                    "- Monitor bookings and respond promptly to any inquiries to build trust and good relationships with players.\n\n" +
                    "If you have any questions, please don't hesitate to reach out to our support team.\n\n" +
                    "Thank you for being a part of SanChoi247!\n\n" +
                    "Best Regards,\n" +
                    "SanChoi247 Team";
    
            emailService.sendEmail(user.getEmail(), subject, body);
        } else {
            userRepo.updateFieldStatus(uid, 0); // Từ chối trường bằng cách đặt trạng thái thành 0
            // Gửi email từ chối
            String subject = "Field Submission Update on SanChoi247";
            String body = "Dear " + user.getName() + ",\n\n" +
                    "We regret to inform you that your field submission, \"" + user.getTen_san() + "\", located at \"" + user.getAddress() + "\", " +
                    "did not meet our requirements for approval at this time.\n\n" +
                    "Here are a few common reasons fields may not be approved:\n" +
                    "- Incomplete or inaccurate information provided.\n" +
                    "- Photos that do not clearly represent the field or its amenities.\n" +
                    "- Issues with the field's location or accessibility.\n\n" +
                    "We encourage you to review your submission, make any necessary adjustments, and resubmit if you believe the field meets our guidelines. " +
                    "You can always reach out to our support team if you need further clarification or assistance.\n\n" +
                    "Thank you for your interest in SanChoi247, and we hope to see your field listed soon.\n\n" +
                    "Best Regards,\n" +
                    "SanChoi247 Team";
emailService.sendEmail(user.getEmail(), subject, body);
        }
        return "redirect:/admin/approveField";
    }
    @GetMapping("admin/pendingRequests")
public ResponseEntity<List<User>> getPendingRequests() {
    try {
        List<User> pendingUsers = userRepo.getUsersByStatus(1); // Status 1 = pending
        return ResponseEntity.ok(pendingUsers);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

// Xử lý phê duyệt/từ chối
@PostMapping("admin/approveFieldOwner")
public ResponseEntity<String> approveFieldOwner(@RequestParam int uid, @RequestParam boolean isApproved) {
    try {
        userRepo.approveFieldOwnerRequest(uid, isApproved);
        User user = userRepo.getUserById(uid);
        
        if (isApproved) {
            // Gửi email chấp nhận
            emailService.sendEmail(user.getEmail(), 
                "Chúc mừng bạn đã trở thành Field Owner",
                "Bạn đã trở thành Field Owner...");
        } else {
            // Gửi email từ chối
            emailService.sendEmail(user.getEmail(), 
                "Yêu cầu Field Owner không được phê duyệt",
                "Yêu cầu của bạn không được phê duyệt...");
        }
        
        return ResponseEntity.ok(statusMessage);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Approval failed.");
    }
}
@GetMapping(value = ("/ViewAllUser"))
public String allUserPage(Model model) throws Exception {
    ArrayList<User> users = adminRepo.getAllUser();
    model.addAttribute("users", users);
    model.addAttribute("status", "approve");
    return "admin/users/view";
}

// Ban user
@PostMapping(value = "/lockUser")
public String lockUsers(@RequestParam("uid") int id, @RequestParam("role") char role, Model model)
        throws Exception {
    adminRepo.lockUser(id, role);
    User user = userRepo.getUserById(id);
    // Gửi email thông báo khóa tài khoản
    String subject = "Your Sanchoi247 Account has been Locked";
    String content = "...";
    emailService.sendEmail(user.getEmail(), subject, content);
    return "admin/users/view";
}

// Unban user
@PostMapping(value = "/unlockUser")
public String unlockUsers(@RequestParam("uid") int id, @RequestParam("role") char role, Model model)
        throws Exception {
    adminRepo.unlockUser(id, role);
    User user = userRepo.getUserById(id);
    // Gửi email thông báo mở khóa tài khoản
    String subject = "Your Sanchoi247 Account has been Unlocked";
    String content = "...";
    emailService.sendEmail(user.getEmail(), subject, content);
    return "admin/users/view";
}

// Xem danh sách user bị ban
@GetMapping(value = ("/ViewAllBanner"))
public String allBannerPage(Model model) throws Exception {
    ArrayList<User> banners = adminRepo.getAllBanner();
    model.addAttribute("status", "reject");
    model.addAttribute("users", banners);
return "admin/users/view";
}
@PostMapping(value = "/stadiumEditPageAdmin")
public String eventEditPage(@RequestParam("stadiumId") int stadiumId, Model model, HttpSession httpSession)
        throws Exception {
    httpSession.setAttribute("stadiumIdEdit", stadiumId);
    User user = userRepo.getUserById(stadiumId);
    model.addAttribute("stadiumEdit", user);
    return "admin/san/editStadiumAdmin";
}
@GetMapping(value = ("/ViewAllStadiumsActive"))
public String allStadiumsOngoingPage(Model model) throws Exception {
    List<User> stadiums = adminRepo.getAllActiveOwner();
    model.addAttribute("stadiums", stadiums);
    return "admin/san/stadiums-active";
}

@GetMapping(value = "/searchEvents")
public String searchEvents(@RequestParam("query") String query, Model model) throws Exception {
    List<User> user = userRepo.searchStadium(query);
    model.addAttribute("stadiums", user);
    return "admin/san/stadiums-active";
}

@GetMapping(value = "admin/dashboard")
public String dashboardPage(Model model) throws Exception {
    AdminStatistics statistics = adminRepo.getStatisticsForAdmin();
    List<TableAdminStatistics> stadiumRevenues = adminRepo.getFieldRevenues();
    List<Double> monthlyRevenue = adminRepo.getMonthlyRevenueForAdmin();
    List<Double> dailyRevenue = adminRepo.getDailyRevenueForAdmin();
    List<TableAdminStatistics> sortedStadiumRevenues = stadiumRevenues.stream()
            .sorted(Comparator.comparingDouble(TableAdminStatistics::getRevenue).reversed())
            .collect(Collectors.toList());

    model.addAttribute("statisticsOfAdmin", statistics);
    model.addAttribute("stadiumRevenues", sortedStadiumRevenues);
    model.addAttribute("monthlyRevenue", monthlyRevenue);
    model.addAttribute("dailyRevenue", dailyRevenue);
    return "admin/dashboard";
}
}