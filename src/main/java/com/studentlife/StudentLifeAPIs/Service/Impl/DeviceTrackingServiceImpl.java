package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterDeviceRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Service.DeviceInfoService;
import com.studentlife.StudentLifeAPIs.Service.DeviceTrackingService;
import com.studentlife.StudentLifeAPIs.Service.UserDeviceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua_parser.Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTrackingServiceImpl implements DeviceTrackingService {

    private final DeviceInfoService deviceInfoService;
    private final UserDeviceService userDeviceService;

    @Override
    public ApiResponse<UserDeviceResponse> getUserDevice(Users user, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = extractClientIp(request);

        // Prefer a client-supplied stable device ID (header) over generating one.
        // Generating a new UUID every request would create a NEW device row each login — bug!
        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isBlank()) {
            // Fallback: derive a quasi-stable ID from UA + IP. Not perfect, but better than random UUID.
            // RED FLAG: this is a workaround — push your frontend to send X-Device-Id.
            deviceId = "ua-" + Integer.toHexString((userAgent + "|" + ipAddress).hashCode());
            log.warn("No X-Device-Id header — falling back to derived id for user {}", user.getId());
        }

        Client client = deviceInfoService.parse(userAgent);

        String browser = client != null && client.userAgent != null ? client.userAgent.family : "Unknown";
        String os      = client != null && client.os != null        ? client.os.family        : "Unknown";
        String deviceName = client != null && client.device != null ? client.device.family    : "Unknown";
        String deviceType = guessDeviceType(userAgent);

        RegisterDeviceRequest req = RegisterDeviceRequest.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .deviceName(deviceName)
                .os(os)
                .browser(browser)
                .build();

        return userDeviceService.registerDevice(user, req, ipAddress);
    }

    /**
     * RED FLAG: getRemoteAddr() returns the proxy IP if you're behind a load balancer (Nginx, ALB, Cloudflare).
     * Always check X-Forwarded-For first, but only trust it if you control the proxy chain.
     */
    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For can be a comma-separated chain; first entry is the original client.
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String guessDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "MOBILE";
        if (ua.contains("ipad") || ua.contains("tablet")) return "TABLET";
        return "DESKTOP";
    }
}