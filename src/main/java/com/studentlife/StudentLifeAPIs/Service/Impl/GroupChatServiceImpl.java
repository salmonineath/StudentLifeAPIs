package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.ChatMessageRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.*;
import com.studentlife.StudentLifeAPIs.Entity.*;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.GroupMessageMapper;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.GroupMessageRepository;
import com.studentlife.StudentLifeAPIs.Service.GroupChatService;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
import com.studentlife.StudentLifeAPIs.Service.PresenceService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.forbidden;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatServiceImpl implements GroupChatService {

    private final GroupMessageRepository groupMessageRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentMemberRepository assignmentMemberRepository;
    private final AuthUtil authUtil;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMessageMapper groupMessageMapper;
    private final OneSignalService oneSignalService;
    private final NotificationService notificationService;
    private final PresenceService presenceService;

    @Override
    public ApiResponse<List<GroupResponse>> getMyGroups() {
        Users currentUser = authUtil.getAuthenticatedUser();
        Long userId = currentUser.getId();

        List<Assignments> allGroups = assignmentRepository.findAllAccessibleByUserId(userId);

        List<GroupResponse> groups = allGroups.stream().map(a -> {

            int memberCount = (int) assignmentMemberRepository
                    .findByAssignmentIdAndStatus(a.getId(), AssignmentMemberStatus.ACCEPTED)
                    .size() + 1;

            String lastMessage = null;
            String lastMessageTime = null;
            String lastMessageSender = null;

            java.util.Optional<GroupMessage> lastMsg = groupMessageRepository
                    .findTopByAssignmentIdOrderByCreatedAtDesc(a.getId());

            if (lastMsg.isPresent()) {
                GroupMessage last = lastMsg.get();
                lastMessage = last.getContent().length() > 50
                        ? last.getContent().substring(0, 50) + "…"
                        : last.getContent();
                lastMessageTime = last.getCreatedAt().toString();
                lastMessageSender = last.getSender().getFullname();
            }

            return GroupResponse.builder()
                    .assignmentId(a.getId())
                    .assignmentTitle(a.getTitle())
                    .subject(a.getSubject())
                    .ownerName(a.getUser().getFullname())
                    .ownerUsername(a.getUser().getUsername())
                    .memberCount(memberCount)
                    .lastMessage(lastMessage)
                    .lastMessageTime(lastMessageTime)
                    .lastMessageSender(lastMessageSender)
                    .build();
        }).toList();

        return new ApiResponse<>(200, true, "Group retrieved successfully", groups);
    }

    @Override
    @Transactional
    public GroupMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {

        Assignments assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> notFound("Assignment not found."));

        // Single lookup — used for both authorization check and sender resolution
        java.util.Optional<AssignmentMember> memberOpt = assignmentMemberRepository
                .findByAssignmentIdAndUserId(request.getAssignmentId(), senderId);

        boolean isOwner = assignment.getUser().getId().equals(senderId);
        boolean isMember = memberOpt.map(m -> m.getStatus() == AssignmentMemberStatus.ACCEPTED).orElse(false);

        if (!isOwner && !isMember) {
            throw forbidden("You are not a member of this group.");
        }

        Users sender;
        if (isOwner) {
            sender = assignment.getUser();
        } else {
            sender = memberOpt.orElseThrow(() -> notFound("Member not found.")).getUser();
        }

        GroupMessage message = GroupMessage.builder()
                .assignmentId(request.getAssignmentId())
                .sender(sender)
                .content(request.getContent())
                .build();

        GroupMessage saved = groupMessageRepository.save(message);
        GroupMessageResponse response = groupMessageMapper.toResponse(saved);

        messagingTemplate.convertAndSend(
                "/topic/group/" + request.getAssignmentId(),
                response
        );

        List<AssignmentMember> members = assignmentMemberRepository
                .findByAssignmentIdAndStatus(request.getAssignmentId(), AssignmentMemberStatus.ACCEPTED);

        if (!assignment.getUser().getId().equals(senderId)) {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle(sender.getFullname());
            notificationRequest.setMessage(request.getContent());
            // Group chat is per-assignment — referenceId is the assignment (group) id; link opens it.
            notificationRequest.setReferenceId(assignment.getId());
            notificationRequest.setLink("/assignments/" + assignment.getId());
            notificationService.sendNotification(notificationRequest, NotificationType.CHAT, assignment.getUser());
        }

        for (AssignmentMember member : members) {
            if (!member.getUser().getId().equals(senderId)) {
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setTitle(sender.getFullname());
                notificationRequest.setMessage(request.getContent());
                notificationRequest.setReferenceId(assignment.getId());
                notificationRequest.setLink("/assignments/" + assignment.getId());
                notificationService.sendNotification(notificationRequest, NotificationType.CHAT, member.getUser());
            }
        }

        log.info("[Chat] Message sent in group {} by user {}", request.getAssignmentId(), senderId);

        return response;
    }

    @Override
    public ApiResponse<PaginatedResponse<GroupMessageResponse>> getChatHistory(Long assignmentId, int page, int size) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        // Check access — must be owner or accepted member
        boolean isOwner = assignment.getUser().getId().equals(currentUser.getId());
        boolean isMember = assignmentMemberRepository
                .findByAssignmentIdAndUserId(assignmentId, currentUser.getId())
                .map(m -> m.getStatus() == AssignmentMemberStatus.ACCEPTED)
                .orElse(false);

        if (!isOwner && !isMember) {
            throw forbidden("You are not a member of this group.");
        }

        // Newest-first paging so page 0 returns the most recent messages.
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PaginatedResponse<GroupMessageResponse> messages = PaginatedResponse.from(
                groupMessageRepository.findByAssignmentId(assignmentId, pageable)
                        .map(groupMessageMapper::toResponse)
        );

        return new ApiResponse<>(200, true, "Chat history retrieved.", messages);
    }

    @Override
    @Transactional
    public ApiResponse<?> clearChatHistory(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("Only the assignment owner can clear chat history.");
        }

        groupMessageRepository.deleteByAssignmentId(assignmentId);
        log.info("[Chat] History cleared for group {} by user {}", assignmentId, currentUser.getId());

        return new ApiResponse<>(200, true, "Chat history cleared");
    }

    @Override
    public ApiResponse<List<MemberResponse>> getGroupMember(Long assignmentId) {

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        Set<Long> onlineIds = presenceService.getOnlineUsers(assignmentId);

        // Accepted members from assignment_members table
        List<AssignmentMember> members = assignmentMemberRepository
                .findByAssignmentIdAndStatus(assignmentId, AssignmentMemberStatus.ACCEPTED);

        List<MemberResponse> responses = new java.util.ArrayList<>();

        // Always add the owner first
        Users owner = assignment.getUser();
        responses.add(MemberResponse.builder()
                .id(owner.getId())
                .fullname(owner.getFullname())
                .username(owner.getUsername())
                .email(owner.getEmail())
                .university(owner.getUniversity())
                .major(owner.getMajor())
                .academicYear(owner.getAcademicYear())
                .online(onlineIds.contains(owner.getId()))
                .build());

        // Then add each accepted member
        for (AssignmentMember m : members) {
            Users u = m.getUser();
            responses.add(MemberResponse.builder()
                    .id(u.getId())
                    .fullname(u.getFullname())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .university(u.getUniversity())
                    .major(u.getMajor())
                    .academicYear(u.getAcademicYear())
                    .online(onlineIds.contains(u.getId()))
                    .build());
        }

        return new ApiResponse<>(
                200,
                true,
                "Get group member successfully",
                responses
        );
    }


    @Override
    public void userJoined(Long assignmentId, Long userId, String username) {
        presenceService.userJoined(assignmentId, userId);
        broadcastPresence(assignmentId);
    }

    @Override
    public void userLeft(Long assignmentId, Long userId, String username) {
        presenceService.userLeft(assignmentId, userId);
        broadcastPresence(assignmentId);
    }

    private void broadcastPresence(Long assignmentId) {
        Set<Long> onlineIds = presenceService.getOnlineUsers(assignmentId);
        PresenceEventResponse eventResponse = PresenceEventResponse.builder()
                .assignmentId(assignmentId)
                .onlineCount(onlineIds.size())
                .onlineUserIds(onlineIds)
                .build();
        messagingTemplate.convertAndSend(
                "/topic/group/" + assignmentId + "/presence", eventResponse
        );
    }

}