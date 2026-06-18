package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;

public interface NotificationService {

    PaginatedResponse<NotificationResponse> getAllNotifications(Long userId, int page, int size);

    ApiResponse<NotificationResponse> sendNotification(NotificationRequest request, NotificationType type, Users recipient);

    void sendRealTimeNotification(Long userId, NotificationResponse notification);

    PaginatedResponse<NotificationResponse> getUnreadNotifications(Long userId, int page, int size);

    long countUnread(Long userId);

    void markAllAsRead(Long userId);

    void markAsRead(Long id, Long userId);

    void deleteNotification(Long id, Long userId);
}
