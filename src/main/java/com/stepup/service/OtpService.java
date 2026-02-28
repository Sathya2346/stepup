package com.stepup.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;

@Service
public class OtpService {
    
    private static class OtpData {
        String otp;
        long expiryTime;
        
        OtpData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
    
    // email -> OtpData
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    // 5 minutes in milliseconds
    private static final long OTP_VALID_DURATION = 5 * 60 * 1000;

    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, new OtpData(otp, System.currentTimeMillis() + OTP_VALID_DURATION));
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpData data = otpStorage.get(email);
        if (data == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > data.expiryTime) {
            otpStorage.remove(email); // Expired, remove it
            return false;
        }
        
        return otp.equals(data.otp);
    }

    public void clearOtp(String email) {
        otpStorage.remove(email);
    }
    
    // Run every 10 minutes to clean up expired OTPs from the map to prevent memory leaks
    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredOtps() {
        long now = System.currentTimeMillis();
        otpStorage.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }
}
