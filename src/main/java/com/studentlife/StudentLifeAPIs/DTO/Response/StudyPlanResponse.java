package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanResponse {
    private Long assignmentId;
    private String plan; // Raw bullet-list text from Gemini
}