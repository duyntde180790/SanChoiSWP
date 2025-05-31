package com.example.SanChoi247.model.repo;

public class UserRepo {
    public void addNewUser(User user) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO users (name, dob, gender, phone, email, username, password, avatar, ten_san, address, img_san1, img_san2, img_san3, img_san4, img_san5, status, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, user.getName());
        ps.setDate(2, user.getDob());
        ps.setString(3, String.valueOf(user.getGender()));
        ps.setString(4, user.getPhone());
        ps.setString(5, user.getEmail());
        ps.setString(6, user.getUsername());
        ps.setString(7, encodedPassword);
        ps.setString(8, user.getAvatar());
        ps.setString(9, user.getTen_san());
        ps.setString(10, user.getAddress());
        ps.setString(11, user.getImg_san1());
        ps.setString(12, user.getImg_san2());
        ps.setString(13, user.getImg_san3());
        ps.setString(14, user.getImg_san4());
        ps.setString(15, user.getImg_san5());
        ps.setInt(16, user.getStatus());
        ps.setString(17, String.valueOf(user.getRole()));
        ps.executeUpdate();
        ps.close();
        con.close();
    }
    
    public boolean existsByUsername(String username) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }
    
    public boolean existsByEmail(String email) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }
    
    public boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password != null && password.matches(passwordPattern);
    }

    public void updatePassword(int uid, String newPassword) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("UPDATE users SET password = ? WHERE uid = ?");
        ps.setString(1, newPassword);
        ps.setInt(2, uid);
        ps.executeUpdate();
        ps.close();
    }
}
