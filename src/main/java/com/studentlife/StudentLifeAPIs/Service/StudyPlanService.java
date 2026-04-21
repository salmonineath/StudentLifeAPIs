package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.StudyPlanResponse;

public interface StudyPlanService {
    ApiResponse<StudyPlanResponse> generateStudyPlan(Long assignmentId);
}
