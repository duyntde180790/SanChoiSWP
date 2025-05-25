package com.example.SanChoi247.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Nhập PasswordEncoder
import org.springframework.stereotype.Repository;

import com.example.SanChoi247.model.entity.User;

@Repository
public class LoginRepo {
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Tiêm PasswordEncoder
    
    public User checkLogin(String username, String password) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null; // Người dùng không tồn tại
            }

            // Lấy mật khẩu đã mã hóa từ cơ sở dữ liệu
            String encodedPassword = rs.getString("password");

            // Kiểm tra xem mật khẩu nhập vào có khớp với mật khẩu đã mã hóa hay không
            if (!passwordEncoder.matches(password, encodedPassword)) {
                return null; // Mật khẩu không hợp lệ
            }

            // Nếu mật khẩu khớp, tạo đối tượng User
            int uid = rs.getInt("uid");
            String name = rs.getString("name");
            Date dob = rs.getDate("dob");
            char gender = rs.getString("gender").charAt(0);
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            String username1 = rs.getString("username");
            String avatar = rs.getString("avatar");
            String ten_san = rs.getString("ten_san");
            String address = rs.getString("address");
            String img_san1 = rs.getString("img_san1");
            String img_san2 = rs.getString("img_san2");
            String img_san3 = rs.getString("img_san3");
            String img_san4 = rs.getString("img_san4");
            String img_san5 = rs.getString("img_san5");
            String cccd_1 = rs.getString("cccd_1");
            String cccd_2 = rs.getString("cccd_2");
            String descript = rs.getString("descript");
            String certificate = rs.getString("certificate");
            String specialization = rs.getString("specialization");
            int exp = rs.getInt("exp");
            String price = rs.getString("price");
            String rate = rs.getString("rate");
            int status = rs.getInt("status");
            char role = rs.getString("role").charAt(0);
            String expirationDate = rs.getString("expirationDate");
            User user = new User(uid, name, dob, gender, phone, email, username1, encodedPassword, avatar, ten_san, address,
                    img_san1, img_san2, img_san3, img_san4, img_san5, cccd_1, cccd_2, descript, certificate, specialization, exp, price, rate, status, role, expirationDate);
            return user;
        }
    }

    
}