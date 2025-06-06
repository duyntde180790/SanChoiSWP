package com.example.SanChoi247.model.repo;

@Repository
public class AdminRepo {
    // Lấy danh sách tất cả user (trừ admin và user bị ban)
    public ArrayList<User> getAllUser() throws Exception {
        String query = "SELECT * FROM users WHERE role != 'A' AND role != 'b' AND role != 'p'";
        // Execute query và map kết quả vào ArrayList<User>
    }

    // Khóa tài khoản user
    public void lockUser(int userId, char currentRole) throws Exception {
        String query;
        if (currentRole == 'U') {
            query = "UPDATE users SET role = 'b' WHERE uid = ?";
        } else if (currentRole == 'C') {
            query = "UPDATE users SET role = 'p' WHERE uid = ?";
        }
        // Execute update query
    }

    // Mở khóa tài khoản user
    public void unlockUser(int userId, char currentRole) throws Exception {
        String query;
        if (currentRole == 'b') {
            query = "UPDATE users SET role = 'U' WHERE uid = ?";
        } else if (currentRole == 'p') {
            query = "UPDATE users SET role = 'C', status = 4 WHERE uid = ?";
        }
        // Execute update query
    }

    // Lấy danh sách user bị ban
    public ArrayList<User> getAllBanner() throws Exception {
        String query = "SELECT * FROM users WHERE role = 'b' OR role = 'p'";
        // Execute query và map kết quả vào ArrayList<User>
    }
}