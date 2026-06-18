package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Entity.PasswordOTP;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.PasswordOTPRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.PasswordOTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class PasswordOTPServiceImpl implements PasswordOTPService {

    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordOTPRepository passwordOTPRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    @Override
    public void sendOtp(String email) {
        // Do not reveal whether the email is registered (prevents account enumeration).
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }
        Users user = userOpt.get();

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        PasswordOTP record = new PasswordOTP();
        record.setEmail(user.getEmail());
        record.setOtpCode(otp);
        record.setCreatedAt(Instant.now());
        record.setExpiredAt(Instant.now().plusSeconds(300));

        passwordOTPRepository.save(record);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your password reset OTP");
        message.setText("Your OTP is: " + otp + "\nIt expires in 5 minutes.");

        javaMailSender.send(message);
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {

        PasswordOTP record = passwordOTPRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> notFound("Password Otp not found for this email"));

        if (record.getUsed()) {
            throw badRequest("OTP already used. Please generate a new OTP and try again.");
        }

        if (Instant.now().isAfter(record.getExpiredAt())) {
            throw badRequest("OTP already expired.");
        }

        if (record.getAttempts() >= MAX_ATTEMPTS) {
            throw badRequest("Too many failed attempts. Please request a new OTP.");
        }

        if (!record.getOtpCode().equals(otpCode)) {
            record.setAttempts(record.getAttempts() + 1);
            passwordOTPRepository.save(record);
            int remaining = MAX_ATTEMPTS - record.getAttempts();
            throw validation("Invalid OTP. " + remaining + " attempts remaining.");
        }

        record.setUsed(true);
        passwordOTPRepository.save(record);

        return true;
    }

    @Override
    public void resetPassword(String email, String otpCode, String newPassword) {
        PasswordOTP record = passwordOTPRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> notFound("No OTP found for this email"));

        if (!record.getUsed()) {
            throw badRequest("OTP did not verify yet");
        }

        if (record.getConsumed()) {
            throw badRequest("This OTP has already been used for a password reset.");
        }

        if (Instant.now().isAfter(record.getExpiredAt())) {
            throw badRequest("OTP has expired. Please request a new one.");
        }

        if (record.getAttempts() >= MAX_ATTEMPTS) {
            throw badRequest("OTP is invalidated due to too many failed attempts.");
        }

        if (!record.getOtpCode().equals(otpCode)) {
            throw validation("Invalid OTP. Please try again");
        }

        Users users = userRepository.findByEmail(email)
                .orElseThrow(() -> notFound("User not found"));

        users.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(users);

        record.setConsumed(true);
        passwordOTPRepository.save(record);
    }
}
