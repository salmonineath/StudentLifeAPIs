package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterDeviceRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.UserDevices;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.DeviceMapper;
import com.studentlife.StudentLifeAPIs.Repository.UserDeviceRepository;
import com.studentlife.StudentLifeAPIs.Service.UserDeviceService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final DeviceMapper deviceMapper;

    @Override
    @Transactional
    public ApiResponse<UserDeviceResponse> registerDevice(Users users, RegisterDeviceRequest request, String ipAddress) {

        if (users == null) {
            throw unauthorized("User is required");
        }
        if (request == null || request.getDeviceId() == null|| request.getDeviceId().isBlank()) {
            throw badRequest("DeviceId is required");
        }

        Optional<UserDevices> existDevice =
                userDeviceRepository.findByUserIdAndDeviceId(
                        users.getId(), request.getDeviceId()
                );

        Instant now = Instant.now();
        UserDevices devices;

        // if a known device is detected update the last seen at without touching the first seen at
        if (existDevice.isPresent()) {
            devices = existDevice.get();
            deviceMapper.updateEntity(request, devices);
            devices.setLastSeenAt(now);
            // if a new ip address is detected refresh it and re active the device
            if (ipAddress !=null) devices.setIpAddress(ipAddress);
            devices.setActive(true);
        } else {
            // Build a new fresh device record if new device is detected
            devices = deviceMapper.toEntity(request);
            devices.setUser(users);
            devices.setIpAddress(ipAddress);
            devices.setFirstSeenAt(now);
            devices.setLastSeenAt(now);
            devices.setActive(true);
        }

        UserDevices saved = userDeviceRepository.save(devices);
        return new ApiResponse<>(
                200,
                true,
                "Device registered successfully.",
                deviceMapper.toResponse(saved)
        );
    }

    @Override
    public ApiResponse<?> removeDevice(Users users, Long deviceId) {
        if (users == null) {
            throw unauthorized("User is required");
        }

        UserDevices devices = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> notFound("Device not found."));

        if (!devices.getUser().getId().equals(users.getId())) {
            throw forbidden("You can't remove this device!");
        }

        devices.setActive(false);
        userDeviceRepository.save(devices);

        return new ApiResponse<>(
                200,
                true,
                "Device is remove successfully."
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDeviceResponse> getMyDevices(Users users) {
        if (users == null) {
            throw unauthorized("User is required");
        }
        return deviceMapper.toResponseList(userDeviceRepository.findAllByUserId(users.getId()));
    }
}
