package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceResponse {

    private Long id;
    private String deviceId;
    private String deviceType;
    private String deviceName;
    private String os;
    private String browser;
    private String ipAddress;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
    private Boolean active;

}
