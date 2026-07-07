package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;

import java.util.List;

public interface NotificationService {

    PaginatedResponse<NotificationResponse> getAllNotifications(Long userId, int page, int size);

    NotificationResponse create(Long userId, String title, String message, NotificationType type);

    NotificationResponse create(Users recipient, String title, String message, NotificationType type);

    default NotificationResponse create(Long userId, String message, NotificationType type) {
        return create(userId, null, message, type);
    }

    ApiResponse<NotificationResponse> sendNotification(NotificationRequest request, NotificationType type, Users recipient);

    void sendRealTimeNotification(Long userId, NotificationResponse notification);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    long countUnread(Long userId);

    void markAllAsRead(Long userId);

    void markAsRead(Long id, Long userId);

    void deleteNotification(Long id, Long userId);
}
