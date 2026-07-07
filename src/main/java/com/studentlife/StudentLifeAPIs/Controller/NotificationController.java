package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/notifications", "/api/v1/notification"})
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications for the authenticated user")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    @GetMapping
    @Operation(summary = "Get notifications", description = "Returns a paginated list of notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getAllNotification(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, capped at 100") @RequestParam(defaultValue = "10") int size
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Notification retrieved",
                notificationService.getAllNotifications(
                        currentUser.getId(),
                        page,
                        size
                )
        ));
    }

    @PostMapping("/send")
    @Operation(summary = "Send a notification", description = "Creates a notification for the authenticated user and sends push/realtime delivery.")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @RequestBody NotificationRequest request,
            @RequestParam NotificationType type
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.status(201).body(notificationService.sendNotification(request, type, currentUser));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Returns unread notifications for the authenticated user.")
    public  ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread() {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Get un read notification successfully",
                notificationService.getUnreadNotifications(currentUser.getId())
        ));
    }

    @GetMapping({"/unread-count", "/unread/count"})
    @Operation(summary = "Get unread notification count", description = "Returns the unread notification count for the authenticated user.")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "Unread count",
                notificationService.countUnread(currentUser.getId())
        ));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks every unread notification for the authenticated user as read.")
    public ResponseEntity<ApiResponse<?>> markAllAsRead() {
        Users currentUser = authUtil.getAuthenticatedUser();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                true,
                "All notifications marked as read."
        ));
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read (legacy)", hidden = true)
    public ResponseEntity<ApiResponse<?>> markAllAsReadLegacy() {
        return markAllAsRead();
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks one notification as read if it belongs to the authenticated user.")
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
    @Operation(summary = "Delete notification", description = "Deletes one notification if it belongs to the authenticated user.")
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
