package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @RequestBody NotificationRequest request,
            @RequestParam NotificationType type
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.status(201).body(notificationService.sendNotification(request, type, currentUser));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread() {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(currentUser.getId()));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread() {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.ok(notificationService.countUnread(currentUser.getId()));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<?>> markAllAsRead() {
        Users currentUser = authUtil.getAuthenticatedUser();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(200, true, "All notifications marked as read.", null));
    }
}
