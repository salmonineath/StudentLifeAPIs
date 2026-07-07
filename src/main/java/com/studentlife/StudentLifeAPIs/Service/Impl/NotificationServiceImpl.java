package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.Entity.Notification;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.NotificationMapper;
import com.studentlife.StudentLifeAPIs.Repository.NotificationRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.badRequest;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.forbidden;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final OneSignalService oneSignalService;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<NotificationResponse> getAllNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Notification> notificationPage = notificationRepository.findByRecipientId(userId, pageable);
        List<NotificationResponse> notifications = notificationPage.getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();

        PaginatedResponse.PaginationMeta paginationMeta = new PaginatedResponse.PaginationMeta(
                notificationPage.getNumber() + 1,
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.hasNext(),
                notificationPage.hasPrevious()
        );

        return new PaginatedResponse<>(notifications, paginationMeta);
    }

    @Override
    @Transactional
    public NotificationResponse create(Long userId, String title, String message, NotificationType type) {
        if (userId == null) {
            throw badRequest("Notification recipient is required.");
        }
        Users recipient = userRepository.findById(userId)
                .orElseThrow(() -> notFound("User not found."));
        return create(recipient, title, message, type);
    }

    @Override
    @Transactional
    public NotificationResponse create(Users recipient, String title, String message, NotificationType type) {
        if (recipient == null || recipient.getId() == null) {
            throw badRequest("Notification recipient is required.");
        }
        if (message == null || message.isBlank()) {
            throw badRequest("Notification message is required.");
        }
        if (type == null) {
            throw badRequest("Notification type is required.");
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(resolveTitle(title, type));
        notification.setMessage(message.trim());
        notification.setType(type);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);
        sendRealTimeNotification(recipient.getId(), response);

        return response;
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> sendNotification(NotificationRequest request, NotificationType type, Users recipient) {
        if (request == null) {
            throw badRequest("Notification request is required.");
        }

        NotificationResponse response = create(recipient, request.getTitle(), request.getMessage(), type);

        oneSignalService.sendPushToUser(
                recipient.getOneSignalPlayerId(),
                resolveTitle(request.getTitle(), type),
                request.getMessage()
        );

        return new ApiResponse<>(
                201,
                true,
                "Message sent successfully.",
                response
        );
    }


    @Override
    public void sendRealTimeNotification(Long userId, NotificationResponse notification) {
        // Sends to: /queue/notifications/{userId}
        // Only the specific user receives this
        messagingTemplate.convertAndSend(
                "/queue/notifications/" + userId,
                notification
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByRecipientIdAndIsReadFalse(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsReadFalse(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> notFound("Notification not found."));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw forbidden("You do not have access to this notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> notFound("Notification not found."));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw forbidden("You do not have access to this notification");
        }
        notificationRepository.delete(notification);
    }

    private String resolveTitle(String title, NotificationType type) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        return switch (type) {
            case INVITE -> "Invitation";
            case DEADLINE, REMINDER -> "Deadline Reminder";
            case CHAT -> "New Chat Message";
            case SYSTEM -> "System Notification";
            default -> "Notification";
        };
    }
}
