package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOTPRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit code")
    private String otpCode;
}
