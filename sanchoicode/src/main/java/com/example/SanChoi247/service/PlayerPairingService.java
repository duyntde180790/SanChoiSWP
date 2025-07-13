package com.example.SanChoi247.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SanChoi247.model.entity.PlayerPairingForm;
import com.example.SanChoi247.model.repo.PlayerPairingFormRepository;

@Service
public class PlayerPairingService {
    private static final Logger log = LoggerFactory.getLogger(PlayerPairingService.class);

    @Autowired
    private PlayerPairingFormRepository formRepository;

    public PlayerPairingForm savePlayerForm(PlayerPairingForm form, String startTime, String endTime) {
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (isPhoneNumberExists(form.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại này đã được đăng ký. Vui lòng nhập số điện thoại khác.");
        }
        
        // Kiểm tra email đã tồn tại chưa
        if (isEmailExists(form.getEmail())) {
            throw new IllegalArgumentException("Email này đã được đăng ký. Vui lòng sử dụng email khác.");
        }
        
        // Validate input
        if (startTime == null || endTime == null || startTime.isEmpty() || endTime.isEmpty()) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        if (form.getAge() <= 0) {
            throw new IllegalArgumentException("Tuổi phải lớn hơn 0");
        }

        try {
            // Định dạng datetime từ chuỗi YYYY-MM-DD HH:mm
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            LocalDateTime now = LocalDateTime.now();

            // Validate thời gian
            if (start.isEqual(end) || start.isAfter(end)) {
                throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
            }
            if (start.isBefore(now)) {
                throw new IllegalArgumentException("Không thể chọn thời gian trong quá khứ");
            }
            if (java.time.Duration.between(start, end).toHours() > 6) {
                throw new IllegalArgumentException("Khoảng thời gian không được vượt quá 6 giờ");
            }

            // Ghi log dữ liệu trước khi lưu
            log.info("Dữ liệu form trước khi lưu: {}", form);
            log.info("startTime: {}, endTime: {}", startTime, endTime);

            // Đặt status mặc định là AVAILABLE (sẵn sàng ghép cặp)
            form.setStatus("AVAILABLE");

            // Format lại thời gian để lưu vào database theo định dạng yêu cầu
            form.setTimeFrame(startTime + "-" + endTime);
            
            // Lưu form và tìm kiếm người chơi phù hợp
            PlayerPairingForm savedForm = formRepository.save(form);
            PlayerPairingForm matchedPlayer = findMatchingPlayer(savedForm);
            
            if (matchedPlayer != null) {
                // Nếu tìm thấy người chơi phù hợp, tự động ghép cặp
                matchPlayers(savedForm, matchedPlayer);
                savedForm.setStatus("MATCHED");
                return formRepository.save(savedForm);
            }
            
            return savedForm;
        } catch (IllegalArgumentException e) {
            log.error("Lỗi khi lưu form: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi định dạng thời gian: {}", e.getMessage());
            throw new IllegalArgumentException("Định dạng thời gian không hợp lệ. Vui lòng dùng định dạng YYYY-MM-DD HH:mm");
        }
    }

    public PlayerPairingForm findMatchingPlayer(PlayerPairingForm currentPlayer) {
        log.info("Bắt đầu tìm kiếm người chơi phù hợp cho player ID: {}", currentPlayer.getId());
        
        // Tìm kiếm người chơi có trạng thái AVAILABLE
        List<PlayerPairingForm> availablePlayers = formRepository.findByStatus("AVAILABLE");
        log.info("Tìm thấy {} người chơi có trạng thái AVAILABLE", availablePlayers.size());
        
        for (PlayerPairingForm player : availablePlayers) {
            // Bỏ qua nếu là chính người chơi hiện tại
            if (player.getId() == currentPlayer.getId()) {
                log.info("Bỏ qua người chơi hiện tại (ID: {})", player.getId());
                continue;
            }
            
            log.info("Kiểm tra người chơi ID: {}", player.getId());
            // Kiểm tra các thuộc tính trùng khớp
            if (isMatchingAttributes(currentPlayer, player)) {
                log.info("Tìm thấy người chơi phù hợp! ID: {}", player.getId());
                return player;
            } else {
                log.info("Người chơi ID: {} không phù hợp", player.getId());
            }
        }
        
        log.info("Không tìm thấy người chơi phù hợp");
        return null;
    }

    private boolean isMatchingAttributes(PlayerPairingForm player1, PlayerPairingForm player2) {
        if (player1 == null || player2 == null) {
            log.info("Một trong hai người chơi là null");
            return false;
        }
        
        // Kiểm tra null cho các trường bắt buộc
        if (player1.getGender() == null || player2.getGender() == null ||
            player1.getMode() == null || player2.getMode() == null ||
            player1.getTimeFrame() == null || player2.getTimeFrame() == null ||
            player1.getLocation() == null || player2.getLocation() == null ||
            player1.getSport() == null || player2.getSport() == null) {
            log.info("Thiếu thông tin bắt buộc cho một trong hai người chơi");
            return false;
        }
        
        // Kiểm tra các trường không bắt buộc
        boolean levelMatch = (player1.getLevel() == null && player2.getLevel() == null) || 
                           (player1.getLevel() != null && player1.getLevel().equals(player2.getLevel()));
        
        boolean goalMatch = (player1.getGoal() == null && player2.getGoal() == null) || 
                          (player1.getGoal() != null && player1.getGoal().equals(player2.getGoal()));
        
        // Log chi tiết từng thuộc tính
        log.info("So sánh thuộc tính giữa player {} và {}:", player1.getId(), player2.getId());
        log.info("Gender: {} vs {} = {}", player1.getGender(), player2.getGender(), player1.getGender().equals(player2.getGender()));
        log.info("Mode: {} vs {} = {}", player1.getMode(), player2.getMode(), player1.getMode().equals(player2.getMode()));
        log.info("TimeFrame: {} vs {} = {}", player1.getTimeFrame(), player2.getTimeFrame(), player1.getTimeFrame().equals(player2.getTimeFrame()));
        log.info("Location: {} vs {} = {}", player1.getLocation(), player2.getLocation(), player1.getLocation().equals(player2.getLocation()));
        log.info("Sport: {} vs {} = {}", player1.getSport(), player2.getSport(), player1.getSport().equals(player2.getSport()));
        log.info("Level match: {}", levelMatch);
        log.info("Goal match: {}", goalMatch);
        
        boolean result = player1.getGender().equals(player2.getGender()) &&
               player1.getMode().equals(player2.getMode()) &&
               player1.getTimeFrame().equals(player2.getTimeFrame()) &&
               player1.getLocation().equals(player2.getLocation()) &&
               player1.getSport().equals(player2.getSport()) &&
               levelMatch &&
               goalMatch;
               
        log.info("Kết quả so khớp: {}", result);
        return result;
    }

    public boolean isPhoneNumberExists(String phone) {
        return formRepository.findByPhone(phone).isPresent();
    }

    public boolean isEmailExists(String email) {
        return formRepository.findByEmail(email).isPresent();
    }

    public void updateStatus(int id, String status) {
        PlayerPairingForm form = formRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người chơi với ID: " + id));
        form.setStatus(status);
        formRepository.save(form);
    }

    public List<PlayerPairingForm> getAllForms() {
        try {
            log.info("Đang lấy danh sách tất cả người chơi");
            List<PlayerPairingForm> forms = formRepository.findAll();
            log.info("Đã lấy được {} người chơi", forms.size());
            return forms;
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách người chơi: ", e);
            throw new RuntimeException("Không thể lấy danh sách người chơi: " + e.getMessage(), e);
        }
    }

    public PlayerPairingForm getPlayerById(Integer id) {
        return formRepository.findById(id).orElse(null);
    }

    public PlayerPairingForm getMatchedPlayer(PlayerPairingForm currentPlayer) {
        try {
            if (currentPlayer == null || currentPlayer.getMatchedWith() == null) {
                log.error("Không tìm thấy thông tin người chơi được ghép cặp");
                return null;
            }
            
            return formRepository.findById(currentPlayer.getMatchedWith())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin người chơi được ghép cặp: ", e);
            return null;
        }
    }

    public void cancelMatch(Integer playerId, String reason) {
        PlayerPairingForm player = getPlayerById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Không tìm thấy người chơi với ID: " + playerId);
        }

        if (!"MATCHED".equals(player.getStatus())) {
            throw new IllegalArgumentException("Người chơi này chưa được ghép cặp");
        }

        // Tìm người chơi được ghép cặp
        PlayerPairingForm matchedPlayer = getMatchedPlayer(player);
        if (matchedPlayer != null) {
            // Cập nhật trạng thái của cả hai người chơi
            player.setStatus("CANCELED");
            matchedPlayer.setStatus("CANCELED");
            
            // Lưu lý do hủy - Chuyển đổi sang ASCII để tránh lỗi Unicode
            String safeReason = convertToASCII(reason);
            player.setCancelReason(safeReason);
            matchedPlayer.setCancelReason("Người chơi khác đã hủy ghép đấu: " + safeReason);
            
            // Lưu thay đổi vào database
            formRepository.save(player);
            formRepository.save(matchedPlayer);
        } else {
            // Nếu không tìm thấy người chơi được ghép cặp, chỉ cập nhật người chơi hiện tại
            player.setStatus("CANCELED");
            player.setCancelReason(convertToASCII(reason));
            formRepository.save(player);
        }
    }

    // Hàm chuyển đổi chuỗi Unicode sang ASCII
    private String convertToASCII(String input) {
        if (input == null) {
            return null;
        }
        
        // Thay thế các ký tự đặc biệt bằng ký tự ASCII tương đương
        return input.replaceAll("[^\\x00-\\x7F]", "")
                   .replaceAll("[^a-zA-Z0-9\\s.,!?-]", "");
    }

    public boolean matchPlayers(PlayerPairingForm player1, PlayerPairingForm player2) {
        try {
            // Kiểm tra trạng thái của cả hai người chơi
            if (!"AVAILABLE".equals(player1.getStatus()) || !"AVAILABLE".equals(player2.getStatus())) {
                log.error("Một trong hai người chơi không ở trạng thái AVAILABLE");
                return false;
            }

            // Cập nhật trạng thái cho người chơi 1
            player1.setStatus("MATCHED");
            player1.setMatchedWith(player2.getId());
            formRepository.save(player1);

            // Cập nhật trạng thái cho người chơi 2
            player2.setStatus("MATCHED");
            player2.setMatchedWith(player1.getId());
            formRepository.save(player2);

            log.info("Ghép đấu thành công giữa người chơi {} và {}", player1.getId(), player2.getId());
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi ghép đấu: ", e);
            return false;
        }
    }

    public PlayerPairingForm getPlayerByEmail(String email) {
        return formRepository.findByEmail(email)
                .orElse(null);
    }

    public PlayerPairingForm createPlayerPairingForm(PlayerPairingForm form) {
        form.setStatus("AVAILABLE");
        // In ra console để debug
        System.out.println("Saving player form with UID: " + form.getUid());
        // Lưu form vào database
        PlayerPairingForm savedForm = formRepository.save(form);
        System.out.println("Saved form with ID: " + savedForm.getId() + " and UID: " + savedForm.getUid());
        return savedForm;
    }

    public PlayerPairingForm savePlayer(PlayerPairingForm player) {
        try {
            return formRepository.save(player);
        } catch (Exception e) {
            log.error("Lỗi khi lưu thông tin người chơi: ", e);
            return null;
        }
    }
}