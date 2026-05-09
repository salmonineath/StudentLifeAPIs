package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterDeviceRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;

import java.util.List;

public interface UserDeviceService {

    ApiResponse<UserDeviceResponse> registerDevice(
            Users users,
            RegisterDeviceRequest request,
            String ipAddress
    );

    List<UserDeviceResponse> getMyDevices(Users users);
}
