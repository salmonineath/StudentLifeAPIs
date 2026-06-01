package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.Entity.Notification;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.NotificationMapper;
import com.studentlife.StudentLifeAPIs.Repository.NotificationRepository;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<NotificationResponse> getAllNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> sendNotification(NotificationRequest request, NotificationType type, Users recipient) {

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(type);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);

        sendRealTimeNotification(recipient.getId(), response);

        oneSignalService.sendPushToUser(
                recipient.getOneSignalPlayerId(),
                request.getTitle(),
                request.getMessage()
        );

        return new ApiResponse<>(
                200,
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
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByRecipientIdAndIsReadFalse(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
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
