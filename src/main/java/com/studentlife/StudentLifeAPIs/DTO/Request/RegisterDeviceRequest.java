package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDeviceRequest {
    private String deviceId;
    private String deviceType;
    private String deviceName;
    private String os;
    private String browser;
    private String ipAddress;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
    private boolean active;
}
