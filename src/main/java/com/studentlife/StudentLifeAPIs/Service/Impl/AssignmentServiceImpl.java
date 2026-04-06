package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.AssignmentMapper;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
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

    @Override
    public ApiResponse<AssignmentResponse> createAssignment(CreateAssignmentRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = new Assignments();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setSubject(request.getSubject());
        assignment.setDueDate(request.getDueDate());
        assignment.setUser(currentUser);

        assignmentRepository.save(assignment);

        return new ApiResponse<>(
                201,
                true,
                "Assignment created successfully.",
                assignmentMapper.toResponse(assignment)
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
}
