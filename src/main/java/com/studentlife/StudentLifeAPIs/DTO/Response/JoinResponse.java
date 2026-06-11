package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinResponse {

    private Long assignmentId;
    private String assignmentTitle;
    private boolean alreadyMember;
}
