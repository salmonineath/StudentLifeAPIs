package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.StudyPlanResponse;
import com.studentlife.StudentLifeAPIs.Service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study-plan")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<StudyPlanResponse>> generate(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(studyPlanService.generateStudyPlan(assignmentId));
    }
}
