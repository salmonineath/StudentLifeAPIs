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
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final OneSignalService oneSignalService;

    @Override
    public PaginatedResponse<NotificationResponse> getAllNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PaginatedResponse.from(
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                        .map(notificationMapper::toResponse)
        );
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> sendNotification(NotificationRequest request, NotificationType type, Users recipient) {

        String link = normalizeLink(request.getLink());

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(type);
        notification.setReferenceId(request.getReferenceId());
        notification.setLink(link);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);

        sendRealTimeNotification(recipient.getId(), response);

        oneSignalService.sendPushToUser(
                recipient.getOneSignalPlayerId(),
                request.getTitle(),
                request.getMessage(),
                saved.getReferenceId(),
                saved.getLink()
        );

        return new ApiResponse<>(
                200,
                true,
                "Message sent successfully.",
                response
        );
    }


    /**
     * Validates that a deep-link is a safe relative in-app path and never an absolute/external URL.
     * Returns null for blank input. Rejects protocol-relative ("//host") and scheme-bearing
     * ("http://", "javascript:") values to avoid open-redirect risk on the frontend.
     */
    private String normalizeLink(String link) {
        if (link == null || link.isBlank()) {
            return null;
        }
        String trimmed = link.trim();
        if (!trimmed.startsWith("/") || trimmed.startsWith("//") || trimmed.contains("://")) {
            throw badRequest("link must be a relative in-app path starting with '/', e.g. \"/assignments/42\".");
        }
        return trimmed;
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
    public PaginatedResponse<NotificationResponse> getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PaginatedResponse.from(
                notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                        .map(notificationMapper::toResponse)
        );
    }

    @Override
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
}
