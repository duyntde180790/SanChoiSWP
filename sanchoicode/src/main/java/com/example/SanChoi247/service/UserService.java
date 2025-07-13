package com.example.SanChoi247.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SanChoi247.model.entity.San;
import com.example.SanChoi247.model.entity.User;
import com.example.SanChoi247.model.repo.Baseconnection;
import com.example.SanChoi247.model.repo.SanRepository;
import com.example.SanChoi247.model.repo.UserRepo;
import com.example.SanChoi247.model.repo.UserRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SanRepository sanRepository;

    public boolean updatePasswordbyEmail(String email, String newPassword) {
        try {
            // Mã hóa mật khẩu mới
            String encodedPassword = passwordEncoder.encode(newPassword);
            // Cập nhật vào cơ sở dữ liệu
            int rowsUpdated = userRepo.updatePasswordByEmail(email, encodedPassword);
            return rowsUpdated > 0; // Trả về true nếu cập nhật thành công
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Trả về false nếu có lỗi
        }
    }
    public List<String> getAllTenSan() {
    return userRepository.findAllTenSan();
}
}  