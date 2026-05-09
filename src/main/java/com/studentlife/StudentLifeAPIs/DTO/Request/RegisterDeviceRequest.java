package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterDeviceRequest {
    private String deviceId;
    private String deviceType;
    private String deviceName;
    private String os;
    private String browser;
}
