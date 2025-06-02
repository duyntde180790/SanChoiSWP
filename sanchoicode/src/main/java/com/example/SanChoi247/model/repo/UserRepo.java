package com.example.SanChoi247.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.example.SanChoi247.model.entity.User;

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
    public User getUserById(int id) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE uid = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
    
        if (rs.next()) { // Kiểm tra nếu có bản ghi
            int uid = rs.getInt("uid");
            String name = rs.getString("name");
            Date dob = rs.getDate("dob");
            char gender = rs.getString("gender").charAt(0);
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            String username = rs.getString("username");
            String password = rs.getString("password");
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
            User user = new User(uid, name, dob, gender, phone, email, username,
             password, avatar, ten_san, address, img_san1, img_san2, img_san3, img_san4, img_san5, cccd_1, cccd_2, descript, certificate , specialization, exp, price, rate, status, role, expirationDate);
            
            rs.close();
            ps.close();
            con.close();
            
            return user; // Trả về đối tượng User nếu có dữ liệu
        } else {
            rs.close();
            ps.close();
            con.close();
            
            return null; // Không có dữ liệu, trả về null hoặc ném ngoại lệ
        }
    }
    public User getUserByUsername(String username) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        
        // Kiểm tra nếu có kết quả
        if (rs.next()) {
            int uid = rs.getInt("uid");
            String name = rs.getString("name");
            Date dob = rs.getDate("dob");
            char gender = rs.getString("gender").charAt(0);
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            String password = rs.getString("password");
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
            String expirationDate = rs.getString("expirationDate");
            char role = rs.getString("role").charAt(0);
            
            // Tạo đối tượng User và trả về
            return new User(uid, name, dob, gender, phone, email, username, password, avatar, ten_san, address,
                    img_san1, img_san2, img_san3, img_san4, img_san5, cccd_1, cccd_2, descript, certificate, specialization, exp, price, rate, status, role, expirationDate);
        } else {
            // Không tìm thấy người dùng
            return null; // Hoặc ném một ngoại lệ nếu bạn muốn xử lý theo cách khác
        }
    }
    public User getUserByEmail(String email) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("select * from users where email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
    
        if (rs.next()) {
            int uid = rs.getInt("uid");
            String name = rs.getString("name");
            Date dob = rs.getDate("dob");
            char gender = rs.getString("gender").charAt(0);
            String phone = rs.getString("phone");
            String email1 = rs.getString("email");
            String username = rs.getString("username");
            String password = rs.getString("password");
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
            // Tạo đối tượng User và trả về
            User user = new User(uid, name, dob, gender, phone, email1, username, password, avatar, ten_san, address, img_san1,
             img_san2, img_san3, img_san4, img_san5, cccd_1, cccd_2, descript, certificate, specialization, exp, price, rate, status, role, expirationDate  );
            rs.close();
            ps.close();
            con.close();
            return user;
        } else {
            // Nếu không tìm thấy người dùng, đóng ResultSet và trả về null hoặc ném ngoại lệ
            rs.close();
            ps.close();
            con.close();
            return null;
        }
    }
    public ArrayList<User> getAllUser() throws Exception {
        ArrayList<User> UserList = new ArrayList<>();
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password)) {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM users");
            
            while (rs.next()) {
                int uid = rs.getInt("uid");
                String name = rs.getString("name");
                Date dob = rs.getDate("dob");
                
                // Kiểm tra gender
                String genderString = rs.getString("gender");
                char gender = (genderString != null && !genderString.isEmpty()) ? genderString.charAt(0) : 'M'; // Giá trị mặc định nếu null
                
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String username = rs.getString("username");
                String password = rs.getString("password");
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
                String rate = rs.getString("rate");                int status = rs.getInt("status");
                String expirationDate = rs.getString("expirationDate");
                // Kiểm tra role
                String roleString = rs.getString("role");
                char role = (roleString != null && !roleString.isEmpty()) ? roleString.charAt(0) : 'U'; // Giá trị mặc định nếu null
                
                User user = new User(uid, name, dob, gender, phone, email, username, password, avatar, ten_san, address,
                        img_san1, img_san2, img_san3, img_san4, img_san5, cccd_1, cccd_2, descript, certificate, specialization, exp, price, rate, status, role, expirationDate);
                
                UserList.add(user);
            }
            
            // Đừng quên đóng kết nối
            rs.close();
            stm.close();
        }
    
        return UserList;
    }
    public ArrayList<User> getAllUserByRole(char role) throws Exception {
        ArrayList<User> UserList = new ArrayList<>();
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("select * from users where role = ?");
        ps.setString(1, String.valueOf(role));
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int uid = rs.getInt("uid");
            String name = rs.getString("name");
            Date dob = rs.getDate("dob");
            char gender = rs.getString("gender").charAt(0);
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            String username = rs.getString("username");
            String password = rs.getString("password");
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
            char role1 = rs.getString("role").charAt(0);
            String expirationDate = rs.getString("expirationDate");
            User user = new User(uid, name, dob, gender, phone, email, username, password, avatar, ten_san, address,
            img_san1, img_san2, img_san3, img_san4, img_san5, cccd_1, cccd_2, descript, certificate, specialization, exp, price, rate, status, role1, expirationDate);
            UserList.add(user);
        }
        return UserList;
    }
     public void editProfile(String name, Date dob, char gender, String phone, String email, int uid) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = null;
        PreparedStatement ps = null;
    
        try {
            con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
            ps = con.prepareStatement(
                    "UPDATE users SET name = ?, dob = ?, gender = ?, phone = ?, email = ? WHERE uid = ?");
            ps.setString(1, name);
            ps.setDate(2, dob);
            ps.setString(3, String.valueOf(gender));
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setInt(6, uid);
    
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("No user found with the given uid: " + uid);
            }
        } finally {
            if (ps != null) ps.close();
            if (con != null) con.close();
        }
    }
    public void updateUserById(User user) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(
                "update users set name = ?, dob = ?, gender = ?, phone = ?, email = ?, username = ?, password = ?, avatar = ?, ten_san = ?, address = ?, img_san1 = ?, img_san2 = ?, img_san3 = ?, img_san4 = ?, img_san5 = ?, descript = ?, certificate = ?, specialization = ?, exp = ?, price = ?, status = ?, role = ? where uid = ?");
        ps.setString(1, user.getName());
        ps.setDate(2, user.getDob());
        ps.setString(3, String.valueOf(user.getGender()));
        ps.setString(4, user.getPhone());
        ps.setString(5, user.getEmail());
        ps.setString(6, user.getUsername());
        ps.setString(7, user.getPassword());
        ps.setString(8, user.getAvatar());
        ps.setString(9, user.getTen_san());
        ps.setString(10, user.getAddress());
        ps.setString(11, user.getImg_san1());
        ps.setString(12, user.getImg_san2());
        ps.setString(13, user.getImg_san3());
        ps.setString(14, user.getImg_san4());
        ps.setString(15, user.getImg_san5());
        ps.setString(16, user.getDescript());
        ps.setString(17, user.getCertificate());
        ps.setString(18, user.getSpecialization());
        ps.setInt(19, user.getExp());
        ps.setString(20, user.getPrice());
        ps.setInt(21, user.getStatus());
        ps.setString(22, String.valueOf(user.getRole()));
        ps.setInt(23, user.getUid());
        ps.executeUpdate();
        ps.close();
    }
    public void save(User user) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String encodedPassword = passwordEncoder.encode(user.getPassword());
        PreparedStatement ps = con.prepareStatement(
            "UPDATE users SET name=?, dob=?, gender=?, phone=?, email=?, username=?, password=?, avatar=?, ten_san=?, address=?, img_san1=?, img_san2=?, img_san3=?, img_san4=?, img_san5=?, status=?, role=? WHERE uid=?");
        
        ps.setString(1, user.getName());
        ps.setDate(2, user.getDob());
        ps.setString(3, String.valueOf(user.getGender()));
        ps.setString(4, user.getPhone());
        ps.setString(5, user.getEmail());
        ps.setString(6, user.getUsername());
        ps.setString(7, encodedPassword); // Lưu mật khẩu đã mã hóa
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
        ps.setInt(18, user.getUid()); // Giả sử bạn có phương thức getUid() trong User để lấy ID của người dùng
        
        ps.executeUpdate();
        ps.close();
        con.close();
    }
     public void editAvatarUser(String avatar, int id) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("UPDATE users SET avatar = ? WHERE uid = ?");
        ps.setString(1, avatar);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }
    
}
