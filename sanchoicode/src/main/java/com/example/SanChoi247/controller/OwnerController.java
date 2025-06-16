package com.example.SanChoi247.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.SanChoi247.model.dto.OwnerStatistics;
import com.example.SanChoi247.model.dto.TableOwnerStatistics;
import com.example.SanChoi247.model.entity.Booking;
import com.example.SanChoi247.model.entity.Booking.PaymentStatus;
import com.example.SanChoi247.model.entity.LoaiSan;
import com.example.SanChoi247.model.entity.San;
import com.example.SanChoi247.model.entity.Size;
import com.example.SanChoi247.model.entity.User;
import com.example.SanChoi247.model.repo.BookingRepo;
import com.example.SanChoi247.model.repo.OwnerRepo;
import com.example.SanChoi247.model.repo.SanRepo;
import com.example.SanChoi247.model.repo.UserRepo;
import com.example.SanChoi247.service.EmailService;
import com.example.SanChoi247.service.FileUploadService;

import jakarta.servlet.http.HttpSession;

public class OwnerController {
    @Autowired
    private FileUploadService fileUpload;
    @Autowired
    SanRepo sanRepo;
    @Autowired
    OwnerRepo ownerRepo;
    @Autowired
    BookingRepo bookingRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    EmailService emailService;

    @GetMapping("/owner/addSmallField")
    public String showAddSmallFieldForm(HttpSession session, Model model) throws Exception {
        User user = (User) session.getAttribute("UserAfterLogin");

        if (user == null) {
            return "auth/login"; // Redirect to login if user is not logged in
        }
        return "owner/addSmallField"; // Direct to the form or message page
    }
    @PostMapping("/owner/addSmallField")
    public String addSmallField(
            @RequestParam("loai_san_id") int loaiSanId,
            @RequestParam("vi_tri_san") String viTriSan,
            @RequestParam("size_id") int sizeId,
            @RequestParam("img") MultipartFile imgFile,
            HttpSession session, Model model) {

        System.out.println("Attempting to add small field...");
        User user = (User) session.getAttribute("UserAfterLogin");

        if (user == null) {
            System.out.println("User not logged in. Redirecting to login.");
            return "auth/login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
        }
        System.out.println("User " + user.getUsername() + " is logged in.");

        try {
            // Validate file
            if (imgFile.isEmpty()) {
                model.addAttribute("message", "Please select an image");
                model.addAttribute("status", "error");
System.out.println("Error: Image file is empty.");
                return "owner/addSmallField";
            }
            System.out.println("Image file received: " + imgFile.getOriginalFilename());

            // Tải lên ảnh và lấy URL
            String imgURL = fileUpload.uploadFile(imgFile);
            if (imgURL == null || imgURL.isEmpty()) {
                model.addAttribute("message", "Failed to upload image to Cloudinary.");
                model.addAttribute("status", "error");
                System.out.println("Error: Image upload to Cloudinary failed, returned null or empty URL.");
                return "owner/addSmallField";
            }
            System.out.println("Image uploaded successfully. URL: " + imgURL);

            // Tạo đối tượng mới cho sân
            San newField = new San();
            newField.setUser(user);
            newField.setLoaiSan(new LoaiSan(loaiSanId));
            newField.setVi_tri_san(viTriSan);
            newField.setSize(new Size(sizeId));
            newField.setImg(imgURL);
            newField.setIs_approve(0); // Trạng thái đang chờ phê duyệt
            newField.setEyeview(0);
            newField.setAverageRating(0.0); // Set default average rating for new field

            System.out.println("Attempting to save new field to DB: " + newField.getVi_tri_san());
            // Gọi phương thức addNewSan để lưu dữ liệu vào cơ sở dữ liệu
            sanRepo.addNewSan(newField);
            System.out.println("addNewSan call completed successfully.");

            model.addAttribute("message", "Small field added successfully and is pending approval.");
            model.addAttribute("status", "success");
            System.out.println("Successfully added field. Redirecting to ViewDetail.");
            return "redirect:/ViewDetail/" + user.getUid(); // Vẫn chuyển hướng khi thành công
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Failed to add the field. Error: " + e.getMessage());
            model.addAttribute("status", "error");
            System.out.println("Caught exception while adding field: " + e.getMessage());
            // Trả về cùng trang form để hiển thị thông báo lỗi
            return "owner/addSmallField";
        }
    }
    @GetMapping("/ViewUserBooking")
    public String viewUserBooking(Model model, HttpSession httpSession) throws Exception {
        User user = (User) httpSession.getAttribute("UserAfterLogin");
        ArrayList<Booking> bookings = ownerRepo.getAllUserBooking(user.getUid());
        model.addAttribute("booking", bookings);
        return "owner/viewBookingOwner";
    }


}