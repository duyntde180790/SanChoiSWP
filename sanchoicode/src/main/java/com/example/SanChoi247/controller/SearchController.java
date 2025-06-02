package com.example.SanChoi247.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.SanChoi247.model.repo.UserRepo;
import com.example.SanChoi247.model.repo.SanRepo;
import com.example.SanChoi247.model.repo.ScheduleBookingRepo;
import com.example.SanChoi247.model.entity.User;
import com.example.SanChoi247.model.entity.San;
import com.example.SanChoi247.model.entity.ScheduleBooking;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    SanRepo sanRepo;
    @Autowired
    ScheduleBookingRepo scheduleBookingRepo;

    public static String removeAccent(String text) {
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    // Tìm kiếm sân theo tên, vị trí, loại sân
    @PostMapping("/SearchSanByTenSan")
    public String searchSanByTenSan(@RequestParam("Search") String Search,
            @RequestParam("Location") String Location,
            @RequestParam("SportType") String SportType,
            Model model) throws Exception {
        ArrayList<User> userList = userRepo.getAllUser();
        ArrayList<User> findSan = new ArrayList<>();

        // Xử lý từ khóa tìm kiếm
        String searchNormalized = removeAccent(Search).toLowerCase();
        String locationNormalized = removeAccent(Location).toLowerCase();
        String sportTypeNormalized = removeAccent(SportType).toLowerCase();
        if (sportTypeNormalized.equals("type")) {
            sportTypeNormalized = "";
        }
        if (locationNormalized.equals("location")) {
            locationNormalized = "";
        }
        String nullValue = "";
        for (User tenSan : userList) {
            boolean matchesSearch = false;
            boolean matchesLocation = false;
            boolean matchesSportType = false;

            // Kiểm tra điều kiện theo tên sân
            if (tenSan.getTen_san() != null && tenSan.getRole() == 'C' && !searchNormalized.equals(nullValue)) {
                String tenSanNormalized = removeAccent(tenSan.getTen_san()).toLowerCase();
                if (tenSanNormalized.contains(searchNormalized)) {
                    matchesSearch = true;
                }
            }

            // Kiểm tra điều kiện theo quận (Location)
            if (tenSan.getAddress() != null && tenSan.getRole() == 'C' && !locationNormalized.equals(nullValue)) {
                String addressNormalized = removeAccent(tenSan.getAddress()).toLowerCase();
                if (addressNormalized.contains(locationNormalized)) {
                    matchesLocation = true;
                }
            }

            // Kiểm tra điều kiện theo loại sân (SportType)
            ArrayList<San> sanlist = sanRepo.getAllSanByChuSanId(tenSan.getUid());
            if (!sportTypeNormalized.equals(nullValue) && tenSan.getRole() == 'C') {
                for (San Sans : sanlist) {
                    String loaiSanNormalized = removeAccent(Sans.getLoaiSan().getLoai_san_type()).toLowerCase();
                    if (loaiSanNormalized.contains(sportTypeNormalized)) {
                        matchesSportType = true;
                        break;
                    }
                }
            }
            if (matchesSearch || matchesLocation || matchesSportType) {
                findSan.add(tenSan);
            }
        }

        if (findSan.isEmpty()) {
            model.addAttribute("message", "not found");
        } else {
            model.addAttribute("userList", findSan);
        }

        return "search/searchResult";
    }

    // Show trang search
    @GetMapping("/ShowSearch")
    public String showSearch() {
        return "search/searchResult";
    }

    // Tìm kiếm sân theo khung giờ
    @PostMapping("/SearchSanTheoKhungGio")
    public String searchSanTheoKhungGio(@RequestParam("SearchDate") String searchDate,
            @RequestParam("StartTime") String startTime,
            @RequestParam("EndTime") String endTime,
            Model model) throws Exception {
        LocalDate date = LocalDate.parse(searchDate);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        ArrayList<San> sanList = sanRepo.getAllSan();
        ArrayList<San> findSan = new ArrayList<>();

        for (San san : sanList) {
            List<ScheduleBooking> bookings = scheduleBookingRepo.findBySanAndDate(san.getSan_id(), date);

            boolean available = false;
            for (ScheduleBooking booking : bookings) {
                if (booking.getStatus().equals("available") &&
                        (start.isAfter(booking.getEnd_time()) || end.isBefore(booking.getStart_time()))) {
                    available = true;
                } else {
                    available = false;
                    break;
                }
            }
            if (available) {
                findSan.add(san);
            }
        }

        if (findSan.isEmpty()) {
            model.addAttribute("message", "No available stadiums found for the selected time.");
        } else {
            model.addAttribute("SanList", findSan);
        }
        return "public/viewDetail";
    }
}