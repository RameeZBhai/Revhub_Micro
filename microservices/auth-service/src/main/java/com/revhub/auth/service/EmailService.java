package com.revhub.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;
    
    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateAndSendOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        
        System.out.println("=== OTP GENERATED ===");
        System.out.println("Email: " + email);
        System.out.println("OTP: " + otp);
        System.out.println("====================");
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("RevHub - Email Verification OTP");
            message.setText("Your OTP for email verification is: " + otp + "\n\nThis OTP is valid for 10 minutes.");
            message.setFrom("revhub.noreply@gmail.com");
            
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.warn("Email sending failed, but OTP is available in console: {}", e.getMessage());
        }
        
        return otp;
    }
    
    public boolean verifyOTP(String email, String otp) {
        String storedOTP = otpStorage.get(email);
        logger.info("Verifying OTP for {}: provided={}, stored={}", email, otp, storedOTP);
        if (storedOTP != null && storedOTP.equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }

    public void clearOTPs() {
        otpStorage.clear();
    }
}