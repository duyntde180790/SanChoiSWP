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
    public AdminStatistics getStatisticsForAdmin() throws Exception {
        Class.forName(Baseconnection.nameClass);
        AdminStatistics stats = new AdminStatistics();

        String sql = "SELECT "
                + "(SELECT SUM(price) FROM booking WHERE status = 0) AS total_sold_amount_active_stadiums, "
                + "(SELECT COUNT(*) FROM users s WHERE s.status = 4) AS total_ongoing_stadiums, "
                + "(SELECT COUNT(*) FROM san s WHERE s.is_approve = 0) AS total_approved_stadiums, "
                + "(SELECT COUNT(*) FROM san s WHERE s.is_approve = 3) AS total_rejected_stadiums, "
                + "(SELECT COUNT(*) FROM refundBooking rb WHERE rb.status = 3) AS total_refund_booking, "
                + "(SELECT COUNT(*) FROM san s WHERE s.is_approve = 1 AND s.eyeview > 0) AS total_passed_stadiums, "
                + "(SELECT COUNT(*) FROM users u WHERE u.role = 'C' AND u.status = 2) AS total_inactive_owners, "
                + "(SELECT COUNT(*) FROM users u WHERE u.role = 'C' AND u.status = 4) AS total_active_owners, "
                + "(SELECT COUNT(*) FROM users u WHERE u.role NOT IN ('a', 'b', 'p')) AS total_users_excluding_owners, "
                + "(SELECT COUNT(*) FROM users u WHERE u.role = 'b' OR u.role = 'p') AS total_banned_users, "
                + "(SELECT COUNT(*) FROM booking b WHERE b.status = 7) AS total_request_refund, "
                + "(SELECT COUNT(*) FROM san s WHERE s.is_approve = 0) AS total_small_field_requests";

        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.setTotalSoldAmountActiveStadiums(rs.getDouble("total_sold_amount_active_stadiums"));
                stats.setTotalOngoingStadiums(rs.getInt("total_ongoing_stadiums"));
                stats.setTotalApprovedStadiums(rs.getInt("total_approved_stadiums"));
                stats.setTotalRejectedStadiums(rs.getInt("total_rejected_stadiums"));
                stats.setTotalRefundBooking(rs.getInt("total_refund_booking"));
                stats.setTotalPassStadiums(rs.getInt("total_passed_stadiums"));
                stats.setTotalInactiveOwners(rs.getInt("total_inactive_owners"));
                stats.setTotalActiveOwners(rs.getInt("total_active_owners"));
                stats.setTotalUsersExcludingOwners(rs.getInt("total_users_excluding_owners"));
                stats.setTotalBannedUsers(rs.getInt("total_banned_users"));
                stats.setTotalRequestRefund(rs.getInt("total_request_refund"));
                stats.setTotalSmallFieldRequests(rs.getInt("total_small_field_requests"));
            }
        }

        return stats;
    }

    public List<TableAdminStatistics> getFieldRevenues() throws Exception {
        Class.forName(Baseconnection.nameClass);
        List<TableAdminStatistics> fieldRevenues = new ArrayList<>(); // Corrected the type here

        String sql = "SELECT u.ten_san AS stadium_name, u.username AS owner_name, SUM(b.price) AS revenue " +
                "FROM booking b " +
                "JOIN san s ON b.san_id = s.san_id " +
                "JOIN users u ON s.uid = u.uid " +
                "WHERE b.status = 0 " + // Added a space after 'status = 0'
                "GROUP BY u.ten_san, u.username " +
                "ORDER BY revenue DESC";

        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TableAdminStatistics stats = new TableAdminStatistics();
                    stats.setStadiumName(rs.getString("stadium_name"));
                    stats.setOwnerName(rs.getString("owner_name"));
                    stats.setRevenue(rs.getDouble("revenue"));

                    fieldRevenues.add(stats);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error retrieving field revenues", e);
        }

        return fieldRevenues;
    }

    public List<Double> getMonthlyRevenueForAdmin() throws Exception {
        List<Double> monthlyRevenue = new ArrayList<>(Collections.nCopies(12, 0.0)); // Initialize list with 12 entries,
                                                                                     // all set to 0.0
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        String sql = "SELECT MONTH(b.date) AS month, SUM(b.price) AS monthly_revenue " +
                "FROM booking b " +
                "JOIN san s ON b.san_id = s.san_id " +
                "WHERE b.status = 0 AND YEAR(b.date) = ? " +
                "GROUP BY MONTH(b.date) " +
                "ORDER BY MONTH(b.date);";

        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentYear); // Set the current year

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("month") - 1; // SQL months start at 1, adjust to 0-based index
                    double revenue = rs.getDouble("monthly_revenue");

                    // Set the revenue in the corresponding position in the list
                    monthlyRevenue.set(month, revenue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Error retrieving monthly revenue for all owners", e);
        }
        return monthlyRevenue;
    }


}