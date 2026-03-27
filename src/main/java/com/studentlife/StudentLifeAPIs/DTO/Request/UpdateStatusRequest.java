package com.studentlife.StudentLifeAPIs.DTO.Request;

import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private AssignmentStatus status;
}