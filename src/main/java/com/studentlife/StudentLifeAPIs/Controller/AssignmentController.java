package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateProgressRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> create(
            @Valid @RequestBody CreateAssignmentRequest request
            ) {
        return ResponseEntity
                .status(201)
                .body(assignmentService.createAssignment(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getMyAssignment() {
        return ResponseEntity.ok(assignmentService.getMyAssignments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProgressRequest request
    ) {
        return ResponseEntity.ok(assignmentService.updateProgress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(assignmentService.deleteAssignment(id));
    }
}
