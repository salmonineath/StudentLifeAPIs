package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;

public interface AssignmentService {
    AssignmentResponse createAssignment(CreateAssignmentRequest request);
}
