package com.studentlife.StudentLifeAPIs.Service;

public interface PasswordOTPService {
    void sendOtp(String email);

    boolean verifyOtp(String email, String otpCode);

    void resetPassword(String email, String otpCode, String newPassword);
}
