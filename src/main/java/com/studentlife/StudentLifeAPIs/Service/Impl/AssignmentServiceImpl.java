package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.AssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.InviteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateProgressRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentMemberResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.InviteResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.JoinResponse;
import com.studentlife.StudentLifeAPIs.Entity.AssignmentMember;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.GroupChatMember;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.AssignmentMapper;
import com.studentlife.StudentLifeAPIs.Repository.*;
import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import com.studentlife.StudentLifeAPIs.Service.EmailService;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final GroupChatMemberRepository groupChatMemberRepository;
    private final AssignmentRepository assignmentRepository;
    private final AuthUtil authUtil;
    private final AssignmentMapper assignmentMapper;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final AssignmentMemberRepository assignmentMemberRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public ApiResponse<AssignmentResponse> createAssignment(AssignmentRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentMapper.toEntity(request);
        assignment.setUser(currentUser);

        assignmentRepository.save(assignment);

        Long scheduleId = scheduleService.createAssignmentSchedule(
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStartDate(),
                assignment.getDueDate(),
                assignment.getId(),
                currentUser
        );

        // ← THE FIX: set scheduleId on the ENTITY before saving, not just the DTO
        assignment.setScheduleId(scheduleId);
        assignmentRepository.save(assignment);

        AssignmentResponse response = assignmentMapper.toResponse(assignment);

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
                .findAllAccessibleByUserId(currentUser.getId())
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

        boolean isOwner = assignment.getUser().getId().equals(currentUser.getId());
        boolean isMember = assignmentMemberRepository.existsByAssignmentIdAndUserIdAndStatus(
                id, currentUser.getId(), AssignmentMemberStatus.ACCEPTED
        );

        if (!isOwner && !isMember) {
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
    @Transactional
    public ApiResponse<AssignmentResponse> updateAssignment(Long id, AssignmentRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> notFound("Assignment not found."));

        boolean isOwner = assignment.getUser().getId().equals(currentUser.getId());

        // Only the owner may modify an assignment. The previous condition
        // (!isOwner && isMember) let any authenticated non-member through.
        if (!isOwner) {
            throw forbidden("You do not have access to this resource.");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setSubject(request.getSubject());
        assignment.setDueDate(request.getDueDate());

        Assignments updated = assignmentRepository.save(assignment);

        return new ApiResponse<>(
                200,
                true,
                "Assignment update successfully.",
                assignmentMapper.toResponse(updated)
        );
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public ApiResponse<?> inviteUser(Long assignmentId, InviteRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("Only the owner can invite members");
        }

        Users invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> notFound("No user found with that email."));

        if (invitedUser.getId().equals(currentUser.getId())) {
            throw validation("You cannot invite yourself.");
        }

        if (assignmentMemberRepository.existsByAssignmentIdAndUserId(assignmentId, invitedUser.getId())) {
            throw validation("This user has already been invited.");
        }

        AssignmentMember member = AssignmentMember.builder()
                .assignment(assignment)
                .user(invitedUser)
                .status(AssignmentMemberStatus.INVITED)
                .inviteToken(UUID.randomUUID().toString())
                .tokenExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        assignmentMemberRepository.save(member);

        emailService.sendInviteEmail(
                invitedUser.getEmail(),
                currentUser.getFullname(),
                assignment.getTitle(),
                assignment.getId(),
                member.getInviteToken()
        );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Assignment Invitation");
        notificationRequest.setMessage(currentUser.getFullname() + " invited you to join \"" + assignment.getTitle() + "\".");
        // Invite refers to an assignment — link directly to it (type=INVITE alone routes to /groups).
        notificationRequest.setReferenceId(assignment.getId());
        notificationRequest.setLink("/assignments/" + assignment.getId());

        notificationService.sendNotification(notificationRequest, NotificationType.INVITE, invitedUser);

        // Surface the invite link so the owner can copy and share it through
        // another channel — kept identical to the accept link used in the email.
        InviteResponse inviteResponse = InviteResponse.builder()
                .email(invitedUser.getEmail())
                .inviteLink(frontendUrl + "/invite/accept?token=" + member.getInviteToken())
                .build();

        return new ApiResponse<>(
                200,
                true,
                "Invitation sent successfully.",
                inviteResponse
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> acceptInvite(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        AssignmentMember member = assignmentMemberRepository
                .findByAssignmentIdAndUserId(assignmentId, currentUser.getId())
                .orElseThrow(() -> notFound("Invitation not found."));

        if (member.getStatus() != AssignmentMemberStatus.INVITED) {
            throw validation("Invitation already responded to.");
        }

        member.setStatus(AssignmentMemberStatus.ACCEPTED);
        assignmentMemberRepository.save(member);

        Assignments assignment = member.getAssignment();
        addToGroupChat(assignment.getId(), assignment.getUser(), currentUser);

        emailService.sendInviteAcceptedEmail(
                assignment.getUser().getEmail(),
                currentUser.getFullname(),
                assignment.getTitle()
        );

        scheduleService.createAssignmentSchedule(
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStartDate(),
                assignment.getDueDate(),
                assignment.getId(),
                currentUser
        );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Invite Accepted");
        notificationRequest.setMessage(currentUser.getFullname() + " accepted your invitation to \"" + assignment.getTitle() + "\".");
        notificationRequest.setReferenceId(assignment.getId());
        notificationRequest.setLink("/assignments/" + assignment.getId());
        notificationService.sendNotification(notificationRequest, NotificationType.INVITE, assignment.getUser());

        return new ApiResponse<>(
                200,
                true,
                "Invite accepted successfully."
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> declineInvite(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        AssignmentMember member = assignmentMemberRepository
                .findByAssignmentIdAndUserId(assignmentId, currentUser.getId())
                .orElseThrow(() -> notFound("Invitation not found."));

        if (member.getStatus() != AssignmentMemberStatus.INVITED) {
            throw validation("Invitation already responded to.");
        }

        member.setStatus(AssignmentMemberStatus.DECLINED);
        assignmentMemberRepository.save(member);

        Assignments assignment = member.getAssignment();

        emailService.sendInviteDeclinedEmail(
                assignment.getUser().getEmail(),
                currentUser.getFullname(),
                assignment.getTitle()
        );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Invite Declined");
        notificationRequest.setMessage(currentUser.getFullname() + " declined your invitation to \"" + assignment.getTitle() + "\".");
        notificationRequest.setReferenceId(assignment.getId());
        notificationRequest.setLink("/assignments/" + assignment.getId());
        notificationService.sendNotification(notificationRequest, NotificationType.INVITE, assignment.getUser());

        return new ApiResponse<>(
                200,
                true,
                "Invite declined successfully."
        );
    }

    @Override
    public ApiResponse<List<AssignmentMemberResponse>> getMembers(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        List<AssignmentMemberResponse> memberResponses = assignmentMemberRepository
                .findByAssignmentIdAndStatus(assignmentId, AssignmentMemberStatus.ACCEPTED)
                .stream()
                .map(m -> AssignmentMemberResponse.builder()
                        .id(m.getId())
                        .userId(m.getUser().getId())
                        .fullname(m.getUser().getFullname())
                        .email(m.getUser().getEmail())
                        .status(m.getStatus())
                        .build())
                .toList();

        return new ApiResponse<>(
                200,
                true,
                "Get all members successfully",
                memberResponses
        );
    }

    @Override
    @Transactional
    public RedirectView processInviteToken(String token, boolean accept) {

        AssignmentMember member = assignmentMemberRepository.findByInviteToken(token)
                .orElse(null);

        if (member == null) {
            return new RedirectView(frontendUrl + "/invite/result?status=invalid");
        }

        if (Instant.now().isAfter(member.getTokenExpiresAt())) {
            return new RedirectView(frontendUrl + "/invite/result?status=expired");
        }

        if (member.getStatus() != AssignmentMemberStatus.INVITED) {
            return new RedirectView(frontendUrl + "/invite/result?status=already_responded");
        }

        Assignments assignment = member.getAssignment();
        Users invitedUser = member.getUser();

        if (accept) {
            member.setStatus(AssignmentMemberStatus.ACCEPTED);
            assignmentMemberRepository.save(member);

            scheduleService.createAssignmentSchedule(
                    assignment.getTitle(),
                    assignment.getDescription(),
                    assignment.getStartDate(),
                    assignment.getDueDate(),
                    assignment.getId(),
                    invitedUser
            );

            addToGroupChat(assignment.getId(), assignment.getUser(), invitedUser);

            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("Invite Accepted");
            notificationRequest.setMessage(invitedUser.getFullname() + " accepted your invitation to \"" + assignment.getTitle() + "\".");
            notificationRequest.setReferenceId(assignment.getId());
            notificationRequest.setLink("/assignments/" + assignment.getId());
            notificationService.sendNotification(notificationRequest, NotificationType.INVITE, assignment.getUser());

            emailService.sendInviteAcceptedEmail(
                    assignment.getUser().getEmail(),
                    invitedUser.getFullname(),
                    assignment.getTitle()
            );

            return new RedirectView(frontendUrl + "/invite/result?status=accepted&assignmentId=" + assignment.getId());
        } else {
            member.setStatus(AssignmentMemberStatus.DECLINED);
            assignmentMemberRepository.save(member);

            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("Invite Declined");
            notificationRequest.setMessage(invitedUser.getFullname() + " declined your invitation to \"" + assignment.getTitle() + "\".");
            notificationRequest.setReferenceId(assignment.getId());
            notificationRequest.setLink("/assignments/" + assignment.getId());
            notificationService.sendNotification(notificationRequest, NotificationType.INVITE, assignment.getUser());

            emailService.sendInviteDeclinedEmail(
                    assignment.getUser().getEmail(),
                    invitedUser.getFullname(),
                    assignment.getTitle()
            );
        }

        return new RedirectView(frontendUrl + "/invite/result?status=declined&assignmentId=" + assignment.getId());
    }

    @Override
    @Transactional
    public ApiResponse<?> getInviteLink(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("Only the owner can create an invite link.");
        }

        // Lazily mint the assignment-level share token the first time it's requested.
        if (assignment.getShareToken() == null || assignment.getShareToken().isBlank()) {
            assignment.setShareToken(UUID.randomUUID().toString());
            assignmentRepository.save(assignment);
        }

        InviteResponse response = InviteResponse.builder()
                .inviteLink(frontendUrl + "/invite/join?token=" + assignment.getShareToken())
                .build();

        return new ApiResponse<>(200, true, "Invite link ready.", response);
    }

    @Override
    @Transactional
    public ApiResponse<?> joinByShareToken(String token) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findByShareToken(token)
                .orElseThrow(() -> notFound("This invite link is not valid."));

        Users owner = assignment.getUser();

        // Owner opening their own link already has full access.
        if (owner.getId().equals(currentUser.getId())) {
            return new ApiResponse<>(200, true, "You already own this assignment.",
                    JoinResponse.builder()
                            .assignmentId(assignment.getId())
                            .assignmentTitle(assignment.getTitle())
                            .alreadyMember(true)
                            .build());
        }

        AssignmentMember member = assignmentMemberRepository
                .findByAssignmentIdAndUserId(assignment.getId(), currentUser.getId())
                .orElse(null);

        // Already an accepted member → idempotent, just send them in.
        if (member != null && member.getStatus() == AssignmentMemberStatus.ACCEPTED) {
            return new ApiResponse<>(200, true, "You're already a member.",
                    JoinResponse.builder()
                            .assignmentId(assignment.getId())
                            .assignmentTitle(assignment.getTitle())
                            .alreadyMember(true)
                            .build());
        }

        // New member, or a previously invited/declined one accepting via the link.
        if (member == null) {
            member = AssignmentMember.builder()
                    .assignment(assignment)
                    .user(currentUser)
                    .build();
        }
        member.setStatus(AssignmentMemberStatus.ACCEPTED);
        assignmentMemberRepository.save(member);

        addToGroupChat(assignment.getId(), owner, currentUser);

        scheduleService.createAssignmentSchedule(
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStartDate(),
                assignment.getDueDate(),
                assignment.getId(),
                currentUser
        );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Invite Accepted");
        notificationRequest.setMessage(currentUser.getFullname() + " joined \"" + assignment.getTitle() + "\".");
        notificationRequest.setReferenceId(assignment.getId());
        notificationRequest.setLink("/assignments/" + assignment.getId());
        notificationService.sendNotification(notificationRequest, NotificationType.INVITE, owner);

        emailService.sendInviteAcceptedEmail(
                owner.getEmail(),
                currentUser.getFullname(),
                assignment.getTitle()
        );

        return new ApiResponse<>(200, true, "Joined assignment successfully.",
                JoinResponse.builder()
                        .assignmentId(assignment.getId())
                        .assignmentTitle(assignment.getTitle())
                        .alreadyMember(false)
                        .build());
    }

    private void addToGroupChat(Long assignmentId, Users owner, Users invitee) {
        // Add owner if not already in chat
        if (!groupChatMemberRepository.existsByAssignmentIdAndUserId(assignmentId, owner.getId())) {
            groupChatMemberRepository.save(
                    GroupChatMember.builder()
                            .assignmentId(assignmentId)
                            .user(owner)
                            .build()
            );
        }
        // Add invitee
        if (!groupChatMemberRepository.existsByAssignmentIdAndUserId(assignmentId, invitee.getId())) {
            groupChatMemberRepository.save(
                    GroupChatMember.builder()
                            .assignmentId(assignmentId)
                            .user(invitee)
                            .build()
            );
        }
    }
}