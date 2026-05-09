package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterDeviceRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.UserDevices;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.UserDeviceRepository;
import com.studentlife.StudentLifeAPIs.Service.UserDeviceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    @Override
    public ApiResponse<UserDeviceResponse> registerDevice(Users users, RegisterDeviceRequest request, String ipAddress) {

        Optional<UserDevices> existDevice =
                userDeviceRepository.findByUserIdAndDeviceId(
                        users.getId(), request.getDeviceId()
                );

        UserDevices devices;

        if (existDevice.isPresent()) {
            devices = existDevice.get();
            devices.setLastSeenAt(Instant.now());
            devices.setFirstSeenAt(Instant.now());
        } else {
            Instant now = Instant.now();
        }

        return null;
    }

    @Override
    public List<UserDeviceResponse> getMyDevices(Users users) {
        return List.of();
    }
}
