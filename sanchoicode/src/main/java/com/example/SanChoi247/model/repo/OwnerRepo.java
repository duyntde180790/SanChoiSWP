package com.example.SanChoi247.model.repo;

public class OwnerRepo {
    public ArrayList<Booking> getAllUserBooking(int uid) throws Exception {
        ArrayList<Booking> bookings = new ArrayList<>();
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);

        String query = "SELECT * FROM booking b " +
                "JOIN san s ON b.san_id = s.san_id " +
                "WHERE (b.status = 0) AND s.uid = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, uid);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Booking booking = new Booking();
            booking.setBooking_id(rs.getInt("booking_id"));
            int sid = rs.getInt("san_id");
            San san = sanRepo.getSanById(sid);
            booking.setSan(san);
            int uid1 = rs.getInt("uid");
            User user = userRepo.getUserById(uid1);
            booking.setUser(user);
            booking.setDate(rs.getTimestamp("date").toLocalDateTime());
            int Sbooking_id = rs.getInt("slot");
            ScheduleBooking scheduleBooking = scheduleBookingRepo.getScheduleBookingById(Sbooking_id);
            booking.setScheduleBooking(scheduleBooking);
            booking.setTotalprice(rs.getDouble("price"));
            booking.setStatus(PaymentStatus.fromInteger(rs.getInt("status")));
            booking.setVnpayData(rs.getString("vnpay_data"));
            bookings.add(booking);
        }

        rs.close();
        ps.close();
        con.close();

        return bookings;
    }

}
