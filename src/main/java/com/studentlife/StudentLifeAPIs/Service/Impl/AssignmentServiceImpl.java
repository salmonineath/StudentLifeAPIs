package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentReminderRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentReminderRepository assignmentReminderRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest request) {

        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignments = Assignments.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .subject(request.getSubject())
                .priority(request.getPriority())
                .status(AssignmentStatus.PENDING)
                .dueDate(request.getDueDate())
                .user(currentUser)
                .build();

        assignments = assignmentRepository.save(assignments);
//        setReminders(assignments, request.getCustomReminderAt());


        return null;
    }
}
