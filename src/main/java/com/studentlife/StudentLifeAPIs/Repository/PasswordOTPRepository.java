package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.PasswordOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordOTPRepository extends JpaRepository<PasswordOTP, Long> {

    Optional<PasswordOTP> findTopByEmailOrderByCreatedAtDesc(String email);
}
