package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    @GetMapping
    public  ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getAllNotification(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Notification retrieved",
                notificationService.getAllNotifications(currentUser.getId(), safePage, safeSize)
        ));
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @jakarta.validation.Valid @RequestBody NotificationRequest request,
            @RequestParam NotificationType type
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.status(201).body(notificationService.sendNotification(request, type, currentUser));
    }

    @GetMapping("/unread")
    public  ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getUnread(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Get un read notification successfully",
                notificationService.getUnreadNotifications(currentUser.getId(), safePage, safeSize)
        ));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Unread count",
                notificationService.countUnread(currentUser.getId())
        ));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<?>> markAllAsRead() {
        Users currentUser = authUtil.getAuthenticatedUser();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "All notifications marked as read."
        ));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(@PathVariable Long id) {
        Users currentUser = authUtil.getAuthenticatedUser();
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Notification marked as read."
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNotification(@PathVariable Long id) {
        Users currentUser = authUtil.getAuthenticatedUser();
        notificationService.deleteNotification(id, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Notification deleted."
        ));
    }
}
