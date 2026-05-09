package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import jakarta.servlet.http.HttpServletRequest;

public interface DeviceTrackingService {
    ApiResponse<UserDeviceResponse> getUserDevice(Users users, HttpServletRequest request);
}
