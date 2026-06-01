package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.DashboardResponse;

public interface DashboardService {
    ApiResponse<DashboardResponse> getDashboard();
}
