package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.OTPRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ResetPasswordRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.VerifyOTPRequest;
import com.studentlife.StudentLifeAPIs.Service.PasswordOTPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class PasswordOTPController {

    private final PasswordOTPService passwordOTPService;

    @PostMapping("/otp-request")
    public ResponseEntity<String> OTPRequest(@Valid @RequestBody OTPRequest request) {
        passwordOTPService.sendOtp(request.getEmail());
        return ResponseEntity.ok("OTP sent successfully. Check your email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOTP(@Valid @RequestBody VerifyOTPRequest request) {
        passwordOTPService.verifyOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok("OTP Verify successfully.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordOTPService.resetPassword(request.getEmail(), request.getOtpCode(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }
}
