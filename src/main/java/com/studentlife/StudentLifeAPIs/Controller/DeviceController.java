package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/devices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeviceController {

    private final UserDeviceService userDeviceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDeviceResponse>>> myDevices(
            @AuthenticationPrincipal Users user) {
        List<UserDeviceResponse> list = userDeviceService.getMyDevices(user);
        return ResponseEntity.ok(new ApiResponse<>(200, true, "Devices retrieved successfully.", list));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<?>> remove(
            @AuthenticationPrincipal Users user,
            @PathVariable Long deviceId) {
        return ResponseEntity.ok(userDeviceService.removeDevice(user, deviceId));
    }
}