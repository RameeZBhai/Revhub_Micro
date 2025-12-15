package com.revhub.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.revhub.auth.service.EmailService;
import com.revhub.auth.entity.User;
import com.revhub.auth.repository.UserRepository;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + request.getUsername());
        System.out.println("Password: " + request.getPassword());

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            System.out.println("User not found for username: " + request.getUsername());
            return ResponseEntity.badRequest().body("User not found. Please register first.");
        }

        User user = userOpt.get();
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Verified: " + user.isVerified());

        if (!user.isVerified()) {
            return ResponseEntity.badRequest().body("Please verify your email first.");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password.");
        }
        String token = generateJwtToken(user.getUsername());
        return ResponseEntity.ok().body("{" +
                "\"token\":\"" + token + "\"," +
                "\"type\":\"Bearer\"," +
                "\"id\":" + user.getId() + "," +
                "\"username\":\"" + user.getUsername() + "\"," +
                "\"email\":\"" + user.getEmail() + "\"" +
                "}");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("=== REGISTRATION ATTEMPT ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Username: " + request.getUsername());

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists. Please login instead.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists. Please choose another.");
        }

        User user = new User(request.getEmail(), request.getUsername(), request.getPassword());
        userRepository.save(user);

        try {
            emailService.generateAndSendOTP(request.getEmail());
            return ResponseEntity.ok("Registration successful. OTP sent to " + request.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration successful but failed to send OTP: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Auth service is running");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody VerifyOTPRequest request) {
        if (emailService.verifyOTP(request.getEmail(), request.getOtp())) {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setVerified(true);
                userRepository.save(user);
                return ResponseEntity.ok("Email verified successfully. You can now login.");
            }
            return ResponseEntity.badRequest().body("User not found.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerification(@RequestParam String email) {
        try {
            emailService.generateAndSendOTP(email);
            return ResponseEntity.ok("OTP sent to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            // Don't reveal user existence, but for now strict checking
            return ResponseEntity.badRequest().body("User with this email does not exist.");
        }
        try {
            emailService.generateAndSendOTP(request.getEmail());
            return ResponseEntity.ok("OTP sent to " + request.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (emailService.verifyOTP(request.getEmail(), request.getOtp())) {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(request.getNewPassword()); // In real app, hash this!
                userRepository.save(user);
                return ResponseEntity.ok("Password reset successfully. You can now login.");
            }
            return ResponseEntity.badRequest().body("User not found.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @DeleteMapping("/clear-data")
    public ResponseEntity<?> clearAllData() {
        userRepository.deleteAll();
        emailService.clearOTPs();
        return ResponseEntity.ok("All data cleared successfully");
    }

    private String generateJwtToken(String username) {
        long expirationTime = System.currentTimeMillis() + 86400000; // 24 hours
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
               java.util.Base64.getEncoder().encodeToString(
                   ("{\"sub\":\"" + username + "\",\"exp\":" + expirationTime + "}").getBytes()
               ) + ".signature";
    }
}

class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

class VerifyOTPRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

class ForgotPasswordRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}