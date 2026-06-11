package com.studentlife.StudentLifeAPIs.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "password_otp")
@Data
public class PasswordOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otpCode;

    private Instant expiredAt;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(nullable = false)
    private Boolean consumed = false;

    private Instant createdAt;
}
