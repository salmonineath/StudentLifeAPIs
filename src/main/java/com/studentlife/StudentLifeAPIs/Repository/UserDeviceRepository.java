package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.UserDevices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevices, Long> {

    Optional<UserDevices> findByUserIdAndDeviceId(
            Long userId,
            String deviceId
    );

    List<UserDevices> findAllByUserId(Long userId);
}
