package main.java.com.example.SanChoi247.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import javax.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;

// TODO: Adjust these imports to match your actual package structure
import com.example.SanChoi247.model.User;
import com.example.SanChoi247.dto.SignUpRequest;
import com.example.SanChoi247.dto.ErrorResponse;
import com.example.SanChoi247.dto.SuccessResponse;

@Controller
public class LoginController {
    // TODO: Replace Object with actual types
    private final Object userRepo;
    private final Object sendOtpToMailService;
    private String generatedOtp;

    public LoginController(Object userRepo, Object sendOtpToMailService) {
        this.userRepo = userRepo;
        this.sendOtpToMailService = sendOtpToMailService;
    }

    @GetMapping("/Signup")
    public String showSignUpForm() {
        return "auth/login";
    }

    @PostMapping("/Signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest, HttpSession httpSession) throws Exception {
        String email = signUpRequest.getEmail();
        String name = signUpRequest.getName();
        String username = signUpRequest.getUsername();
        String password = signUpRequest.getPassword();
        String confirmPassword = signUpRequest.getConfirmPassword();
        char role = 'U';
        char gender = signUpRequest.getGender();

        if (userRepo.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists."));
        }
        if (userRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email already exists."));
        }
        if (!userRepo.isValidPassword(password)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Password invalid."));
        }
        if (!userRepo.isValidPassword(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password invalid.");
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(Character.toUpperCase(role));
        newUser.setGender(gender);

        if (name != null && !name.isEmpty()) {
            String avatarDefault = String.valueOf(name.charAt(0)).toUpperCase();
            newUser.setAvatar(avatarDefault);
        } else {
            newUser.setAvatar("U");
        }

        if (name != null && !name.isEmpty()) {
            String avatarUrl = "https://ui-avatars.com/api/?name=" + name.charAt(0) + "&background=random";
            newUser.setAvatar(avatarUrl);
        } else {
            newUser.setAvatar("https://ui-avatars.com/api/?name=U&background=random");
        }
        if (newUser.getName() == null || newUser.getEmail() == null || newUser.getUsername() == null
                || newUser.getPassword() == null) {
            throw new Exception("Name, Email, or Username cannot be null.");
        }
        httpSession.setAttribute("newUser", newUser);

        try {
            generatedOtp = sendOtpToMailService.sendOtpService(email);
            return ResponseEntity.ok(new SuccessResponse("OTP sent to your email.", "auth/enterOtp"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/verifyOtp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession httpSession, Model model) throws Exception {
        User newUser = (User) httpSession.getAttribute("newUser");
        if (newUser == null) {
            model.addAttribute("error", "user null");
            return "auth/enterOtp";
        }
        if (otp == null || otp.trim().isEmpty()) {
            model.addAttribute("error", "OTP không được để trống");
            return "auth/enterOtp";
        }
        if (!generatedOtp.equals(otp)) {
            model.addAttribute("error", "OTP không chính xác");
            return "auth/enterOtp";
        }
        if (otp.equals(generatedOtp)) {
            try {
                userRepo.addNewUser(newUser);
                httpSession.invalidate();
                return "auth/login";
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Error saving user: " + e.getMessage());
                return "auth/enterOtp";
            }
        } else {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "auth/enterOtp";
        }
    }

    @GetMapping("/Login")
    public String showLogin(Model model) {
    return "auth/login";
}

    @PostMapping("/LoginToSystem")
    public ResponseEntity<?> loginToSystem(@RequestParam("username") String username,
        @RequestParam("password") String password,
        HttpSession httpSession) throws Exception {
    User user = loginRepo.checkLogin(username, password);

    if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password"));
    } else {
        String token = jwtTokenProvider.generateToken(user);

        httpSession.setAttribute("accessToken", token);
        httpSession.setAttribute("UserAfterLogin", user);
        httpSession.setAttribute("userRole", user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("username", user.getUsername());
        response.put("uid", user.getUid());
        response.put("avatar", user.getAvatar());
        response.put("name", user.getName());

        String redirect = (String) httpSession.getAttribute("redirect");
        if (redirect != null) {
            httpSession.removeAttribute("redirect");
            response.put("redirect", redirect);
        } else if (user.getRole() == 'A') {
            response.put("redirect", "/admin/dashboard");
        } else if (user.getRole() == 'p' || user.getRole() == 'b') {
            response.put("redirect", "/auth/banned");
        } else {
            response.put("redirect", "/");
        }

        return ResponseEntity.ok(response);
    }
}

@GetMapping("/Logout")
public String logout(HttpSession httpSession) {
    httpSession.removeAttribute("UserAfterLogin");
    httpSession.invalidate();
    return "redirect:/";
}

@PostMapping("/UserAfterLogin")
@ResponseBody
public ResponseEntity<?> afterLoginWithGG(HttpServletRequest request, Authentication authentication)
        throws Exception {
    if (authentication == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }

    HttpSession session = request.getSession();
    DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();

    String email = (String) oauthUser.getAttribute("email");
    String name = (String) oauthUser.getAttribute("name");
    String avatar = (String) oauthUser.getAttribute("picture");

    User existingUser = userRepo.getUserByEmail(email);

    if (existingUser == null) {
        User newUser = createUser(email, name, avatar);
        userRepo.addNewUser(newUser);
        existingUser = newUser;
    } else {
        existingUser.setAvatar(avatar);
        userRepo.save(existingUser);
    }

    int uid = existingUser.getUid();
    String token = jwtTokenProvider.generateToken(existingUser);

    session.setAttribute("UserAfterLogin", existingUser);
    session.setAttribute("accessToken", token);
    session.setAttribute("userRole", existingUser.getRole());

    Map<String, Object> response = new HashMap<>();
    response.put("accessToken", token);
    response.put("username", existingUser.getUsername());
    response.put("uid", uid);
    response.put("avatar", avatar);
    response.put("name", name);

    String redirect = (String) session.getAttribute("redirect");
    if (redirect != null) {
        session.removeAttribute("redirect");
        response.put("redirect", redirect);
    } else if (existingUser.getRole() == 'A') {
        response.put("redirect", "/admin/dashboard");
    } else if (existingUser.getRole() == 'p' || existingUser.getRole() == 'b') {
        response.put("redirect", "/auth/banned");
    } else {
        response.put("redirect", "/");
    }

    return ResponseEntity.ok(response);
}

@PostMapping("/resendOtp")
  public String resendOtp(Model model) {
      try {
          generatedOtp = sendOtpToMailService.sendOtpService(email);
          model.addAttribute("success", "OTP đã được gửi lại.");
          model.addAttribute("email", email);
          return "auth/enterOtp";
      } catch (RuntimeException e) {
          model.addAttribute("error", e.getMessage());
          return "auth/login";
      }
    }

private User createUser(String email, String name, String avatar) {
    User newUser = new User();
    newUser.setEmail(email);
    newUser.setUsername(email);
    newUser.setName(name);
    newUser.setAvatar(avatar);
    newUser.setPassword(new BCryptPasswordEncoder().encode("defaultPassword"));
    newUser.setStatus(0);
    newUser.setRole('U');
    return newUser;
}

}