package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateProgressRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Mapper.AssignmentMapper;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.forbidden;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AuthUtil authUtil;
    private final AssignmentMapper assignmentMapper;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;

    @Override
    public ApiResponse<AssignmentResponse> createAssignment(CreateAssignmentRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentMapper.toEntity(request);
        assignment.setUser(currentUser);

        assignmentRepository.save(assignment);

        Long scheduleId = scheduleService.createAssignmentSchedule(
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getId(),
                currentUser
        );

        AssignmentResponse response = assignmentMapper.toResponse(assignment);
        response.setScheduleId(scheduleId);

        return new ApiResponse<>(
                201,
                true,
                "Assignment created successfully.",
                response
        );
    }

    @Override
    public ApiResponse<List<AssignmentResponse>> getMyAssignments() {

        Users currentUser = authUtil.getAuthenticatedUser();

        List<AssignmentResponse> responses = assignmentRepository
                .findByUserId(currentUser.getId())
                .stream()
                .map(assignmentMapper::toResponse)
                .toList();

        return new ApiResponse<>(
                200,
                true,
                "Get all assignment successfully.",
                responses
        );
    }

    @Override
    public ApiResponse<AssignmentResponse> getAssignmentById(Long id) {

        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        return new ApiResponse<>(
                200,
                true,
                "Get assignment successfully.",
                assignmentMapper.toResponse(assignment)
        );
    }

    @Override
    public ApiResponse<AssignmentResponse> updateProgress(Long id, UpdateProgressRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        assignment.setProgress(request.getProgress());

        if (request.getProgress() == 100) {
            assignment.setStatus(AssignmentStatus.COMPLETED);
        } else if (request.getProgress() > 0) {
            assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        } else {
            assignment.setStatus(AssignmentStatus.PENDING);
        }

        assignmentRepository.save(assignment);

        return new ApiResponse<>(
                200,
                true,
                "Progress updated successfully.",
                assignmentMapper.toResponse(assignment)
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteAssignment(Long id) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        scheduleRepository.deleteByAssignmentId(id);
        assignmentRepository.delete(assignment);

        return new ApiResponse<>(
                200,
                true,
                "Assignment deleted successfully."
        );
    }
}
