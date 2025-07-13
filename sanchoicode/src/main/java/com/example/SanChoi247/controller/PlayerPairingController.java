package com.example.SanChoi247.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.SanChoi247.model.entity.PlayerPairingForm;
import com.example.SanChoi247.model.entity.User;
import com.example.SanChoi247.model.repo.PlayerPairingFormRepository;
import com.example.SanChoi247.service.PlayerPairingService;
import com.example.SanChoi247.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class PlayerPairingController {
    private static final Logger log = LoggerFactory.getLogger(PlayerPairingController.class);

    @Autowired
    private PlayerPairingFormRepository formRepository;
@Autowired
    private PlayerPairingService playerPairingService;
    @Autowired
    private UserService userService;

    @GetMapping("/playerPairing")
    public String getPlayerPairing(Model model, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("playerInfo", new PlayerPairingForm());
        model.addAttribute("locations", userService.getAllTenSan());
        return "user/playerPairing";
    }

    @PostMapping("/savePlayerForm")
    public String saveForm(@Valid @ModelAttribute("playerInfo") PlayerPairingForm form,
                          BindingResult result,
                          @RequestParam("startTime") String startTime,
                          @RequestParam("endTime") String endTime,
                          Model model,
                          HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("error", result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Dữ liệu không hợp lệ"));
            model.addAttribute("locations", userService.getAllTenSan());
            return "user/playerPairing";
        }

        try {
            // Set uid từ user trong session
            form.setUid(user.getUid());
            log.info("Setting UID from session: {}", user.getUid());
            
            // Lưu form với uid
            playerPairingService.savePlayerForm(form, startTime, endTime);
            return "redirect:/playerPairingList";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("locations", userService.getAllTenSan());
            return "user/playerPairing";
        }
    }

    @GetMapping("/playerPairingList")
    public String showPlayerPairingList(Model model, HttpSession session) {
        try {
            // Kiểm tra user đã đăng nhập chưa
            User user = (User) session.getAttribute("UserAfterLogin");
            if (user == null) {
                return "redirect:/login";
            }

            List<PlayerPairingForm> players = playerPairingService.getAllForms();
            model.addAttribute("players", players);
            
            // Lấy thông tin người dùng hiện tại từ session
            String currentUserEmail = (String) session.getAttribute("userEmail");
            if (currentUserEmail != null) {
                PlayerPairingForm currentUser = playerPairingService.getPlayerByEmail(currentUserEmail);
                if (currentUser != null) {
                    model.addAttribute("currentPlayerId", currentUser.getId());
                }
            }
            return "user/playerPairingList";
        } catch (Exception e) {
            log.error("Lỗi khi hiển thị danh sách người chơi: ", e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách người chơi: " + e.getMessage());
            return "user/playerPairingList";
        }
    }

    @GetMapping("/playerPairingList/{id}")
    public String showPlayerDetailsInList(@PathVariable("id") Integer id, Model model, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        PlayerPairingForm player = playerPairingService.getPlayerById(id);
        if (player == null) {
            model.addAttribute("error", "Không tìm thấy thông tin người chơi");
            return "user/playerPairingList";
        }
        
        List<PlayerPairingForm> players = playerPairingService.getAllForms();
        model.addAttribute("players", players);
        session.setAttribute("currentPlayerId", id);
        
        String currentUserEmail = (String) session.getAttribute("userEmail");
        model.addAttribute("currentUserEmail", currentUserEmail);
        return "user/playerPairingList";
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam("id") int id, @RequestParam("status") String status, Model model, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            playerPairingService.updateStatus(id, status);
            return "redirect:/playerPairingList";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("players", playerPairingService.getAllForms());
            return "user/playerPairingList";
        }
    }

    @GetMapping("/pair/{id}")
    public String pairPlayer(@PathVariable Integer id, Model model, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            PlayerPairingForm player = playerPairingService.getPlayerById(id);
            if (player == null) {
            model.addAttribute("error", "Không tìm thấy người chơi");
                return "redirect:/playerPairingList";
            }

            PlayerPairingForm matchedPlayer = playerPairingService.findMatchingPlayer(player);
            if (matchedPlayer != null) {
                playerPairingService.matchPlayers(player, matchedPlayer);
                model.addAttribute("success", "Đã ghép cặp thành công!");
            } else {
                model.addAttribute("error", "Không tìm thấy người chơi phù hợp");
            }

            session.setAttribute("currentPlayerId", id);
            return "redirect:/playerPairingList";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/playerPairingList";
        }
    }

    @GetMapping("/playerPairingDetails/{id}")
    public String showPlayerDetails(@PathVariable Integer id, Model model, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            PlayerPairingForm player = playerPairingService.getPlayerById(id);
            if (player == null) {
                model.addAttribute("error", "Không tìm thấy thông tin người chơi");
                return "error";
            }

            String currentUserEmail = (String) session.getAttribute("userEmail");
            if (currentUserEmail != null) {
                PlayerPairingForm currentUser = playerPairingService.getPlayerByEmail(currentUserEmail);
                if (currentUser != null) {
                    model.addAttribute("currentPlayerId", currentUser.getId());
                }
            }

            // Thêm danh sách sân vào model
            model.addAttribute("locations", userService.getAllTenSan());
            model.addAttribute("player", player);
            return "user/playerPairingDetails";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/match/{id}")
    public String matchPlayer(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Lấy thông tin người chơi hiện tại
            PlayerPairingForm currentPlayer = playerPairingService.getPlayerById(id);
            if (currentPlayer == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người chơi");
                return "redirect:/playerPairingList";
            }

            // Kiểm tra xem người chơi có phải là người dùng hiện tại không
            if (currentPlayer.getUid() == null || !currentPlayer.getUid().equals(user.getUid())) {
                redirectAttributes.addFlashAttribute("error", "Bạn chỉ có thể ghép đấu với người chơi khác");
                return "redirect:/playerPairingList";
            }

            // Kiểm tra trạng thái của người chơi
            if (!"AVAILABLE".equals(currentPlayer.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Người chơi không ở trạng thái sẵn sàng để ghép đấu");
                return "redirect:/playerPairingList";
            }

            // Tìm người chơi phù hợp
            PlayerPairingForm matchedPlayer = playerPairingService.findMatchingPlayer(currentPlayer);
            if (matchedPlayer == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người chơi phù hợp");
                return "redirect:/playerPairingList";
            }

            // Thực hiện ghép cặp
            playerPairingService.matchPlayers(currentPlayer, matchedPlayer);
            redirectAttributes.addFlashAttribute("success", "Ghép đấu thành công!");
            
            // Chuyển hướng đến trang thông tin trận đấu
            return "redirect:/match/info/" + currentPlayer.getId();
        } catch (Exception e) {
            log.error("Lỗi khi ghép cặp người chơi: ", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/playerPairingList";
        }
    }

    @GetMapping("/quickMatch/{id}")
    public String showQuickMatchForm(@PathVariable Integer id, Model model, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Lấy thông tin người chơi đối thủ
            PlayerPairingForm opponentPlayer = playerPairingService.getPlayerById(id);
            if (opponentPlayer == null) {
                model.addAttribute("error", "Không tìm thấy thông tin người chơi");
                return "redirect:/playerPairingList";
            }

            // Kiểm tra trạng thái của người chơi đối thủ
            if (!"AVAILABLE".equals(opponentPlayer.getStatus())) {
                model.addAttribute("error", "Người chơi không ở trạng thái sẵn sàng để ghép đấu");
                return "redirect:/playerPairingList";
            }

            // Tạo form mới cho người chơi hiện tại
            PlayerPairingForm newPlayerForm = new PlayerPairingForm();
            newPlayerForm.setUid(user.getUid());
            newPlayerForm.setStatus("AVAILABLE");
            
            // Lấy thông tin từ người chơi đối thủ để điền vào form
            newPlayerForm.setSport(opponentPlayer.getSport());
            newPlayerForm.setMode(opponentPlayer.getMode());
            newPlayerForm.setTimeFrame(opponentPlayer.getTimeFrame());
            newPlayerForm.setLocation(opponentPlayer.getLocation());
            newPlayerForm.setPlayType(opponentPlayer.getPlayType());
            
            // Thêm thông tin vào model
            model.addAttribute("playerInfo", newPlayerForm);
            model.addAttribute("opponentPlayer", opponentPlayer);
            model.addAttribute("locations", userService.getAllTenSan());
            
            // Lưu ID của người chơi đối thủ vào session để sử dụng sau này
            session.setAttribute("opponentPlayerId", id);
            
            return "user/quickMatchForm";
        } catch (Exception e) {
            log.error("Lỗi khi hiển thị form ghép đấu nhanh: ", e);
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/playerPairingList";
        }
    }

    @PostMapping("/saveQuickMatch")
    public String saveQuickMatch(@ModelAttribute("playerInfo") PlayerPairingForm form,
            HttpSession session,
            Model model) {
        try {
            // Kiểm tra đăng nhập
            User user = (User) session.getAttribute("UserAfterLogin");
            if (user == null) {
                log.warn("User not logged in, redirecting to login page");
                return "redirect:/login";
            }

            // Lấy thông tin đối thủ từ session
            Integer opponentId = (Integer) session.getAttribute("opponentPlayerId");
            if (opponentId == null) {
                log.error("No opponent ID found in session");
                model.addAttribute("error", "Không tìm thấy thông tin đối thủ");
                return "user/quickMatchForm";
            }

            // Lấy thông tin form của đối thủ
            PlayerPairingForm opponentForm = playerPairingService.getPlayerById(opponentId);
            if (opponentForm == null) {
                log.error("No opponent form found for ID: {}", opponentId);
                model.addAttribute("error", "Không tìm thấy thông tin đối thủ");
                return "user/quickMatchForm";
            }

            // Lấy thông tin sân từ form của đối thủ
            String location = opponentForm.getLocation();
            if (location == null || location.isEmpty()) {
                log.error("No location found in opponent form");
                model.addAttribute("error", "Không tìm thấy thông tin sân");
                return "user/quickMatchForm";
            }

            // Lưu form của người chơi hiện tại
            form.setUid(user.getUid());
            form.setStatus("AVAILABLE");
            PlayerPairingForm savedForm = playerPairingService.savePlayer(form);
            if (savedForm == null) {
                log.error("Failed to save player form");
                model.addAttribute("error", "Không thể lưu thông tin người chơi");
                return "user/quickMatchForm";
            }

            // Thực hiện ghép đấu
            boolean matchSuccess = playerPairingService.matchPlayers(savedForm, opponentForm);
            if (!matchSuccess) {
                log.error("Failed to match players");
                model.addAttribute("error", "Không thể ghép đấu với người chơi này");
                return "user/quickMatchForm";
            }

            // Lấy thông tin chủ sân và chuyển hướng
            User courtOwner = userService.getUserByTenSan(location);
            if (courtOwner != null) {
                log.info("Found court owner with UID: {}, redirecting to ViewDetail", courtOwner.getUid());
                return "redirect:/ViewDetail/" + courtOwner.getUid();
            }

            log.error("No court owner found for location: {}", location);
            model.addAttribute("error", "Không tìm thấy thông tin sân");
            return "user/quickMatchForm";
        } catch (Exception e) {
            log.error("Error in saveQuickMatch: ", e);
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "user/quickMatchForm";
        }
    }
    @GetMapping("/pairingInformation")
    public String showPairingInformation(Model model, HttpSession session) {
        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            PlayerPairingForm currentPlayer = (PlayerPairingForm) model.getAttribute("currentPlayer");
            PlayerPairingForm matchedPlayer = (PlayerPairingForm) model.getAttribute("matchedPlayer");
            
            if (currentPlayer == null || matchedPlayer == null) {
                return "redirect:/playerPairingList";
            }
            
            model.addAttribute("currentPlayer", currentPlayer);
            model.addAttribute("matchedPlayer", matchedPlayer);
            return "user/pairingInformation";
        } catch (Exception e) {
            log.error("Lỗi khi hiển thị thông tin ghép cặp: ", e);
            return "redirect:/playerPairingList";
        }
    }
}