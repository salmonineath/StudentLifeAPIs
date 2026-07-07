package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.Entity.Notification;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Exception.ApiException;
import com.studentlife.StudentLifeAPIs.Mapper.NotificationMapper;
import com.studentlife.StudentLifeAPIs.Repository.NotificationRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.Impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private OneSignalService oneSignalService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Users user;
    private Notification notification;
    private NotificationResponse response;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L)
                .fullname("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .oneSignalPlayerId("player-1")
                .build();

        notification = Notification.builder()
                .id(10L)
                .recipient(user)
                .title("Deadline Reminder")
                .message("Assignment is due soon.")
                .type(NotificationType.DEADLINE)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        response = new NotificationResponse(
                10L,
                1L,
                "Deadline Reminder",
                "Assignment is due soon.",
                NotificationType.DEADLINE,
                false,
                notification.getCreatedAt()
        );
    }

    @Test
    @DisplayName("returns paginated notifications scoped to a user")
    void getAllNotifications_returnsPaginatedUserNotifications() {
        when(notificationRepository.findByRecipientId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 10), 1));
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        PaginatedResponse<NotificationResponse> result = notificationService.getAllNotifications(1L, 0, 10);

        assertThat(result.getItems()).containsExactly(response);
        assertThat(result.getPagination().getCurrentPage()).isEqualTo(1);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(1);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findByRecipientId(eq(1L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("create persists an unread notification and emits it in-app")
    void create_savesUnreadNotificationAndEmitsRealtimeNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(response);

        NotificationResponse result = notificationService.create(1L, null, "Assignment is due soon.", NotificationType.DEADLINE);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getRecipient()).isEqualTo(user);
        assertThat(saved.getTitle()).isEqualTo("Deadline Reminder");
        assertThat(saved.getMessage()).isEqualTo("Assignment is due soon.");
        assertThat(saved.getType()).isEqualTo(NotificationType.DEADLINE);
        assertThat(saved.isRead()).isFalse();
        verify(messagingTemplate).convertAndSend("/queue/notifications/1", response);
    }

    @Test
    @DisplayName("sendNotification saves before sending push notification")
    void sendNotification_savesThenSendsPush() {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Assignment Invitation");
        request.setMessage("Join this assignment.");

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        ApiResponse<NotificationResponse> result = notificationService.sendNotification(
                request,
                NotificationType.INVITE,
                user
        );

        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.getData()).isEqualTo(response);

        InOrder inOrder = inOrder(notificationRepository, oneSignalService);
        inOrder.verify(notificationRepository).save(any(Notification.class));
        inOrder.verify(oneSignalService).sendPushToUser("player-1", "Assignment Invitation", "Join this assignment.");
    }

    @Test
    @DisplayName("countUnread delegates to the user-scoped repository count")
    void countUnread_returnsRepositoryCount() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(3L);

        assertThat(notificationService.countUnread(1L)).isEqualTo(3L);
    }

    @Test
    @DisplayName("markAsRead updates only notifications owned by the user")
    void markAsRead_ownedNotification_setsReadTrue() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(10L, 1L);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead rejects notifications owned by another user")
    void markAsRead_otherUserNotification_throwsForbidden() {
        Users otherUser = Users.builder()
                .id(2L)
                .fullname("Other User")
                .username("other")
                .email("other@example.com")
                .password("encoded-password")
                .build();
        notification.setRecipient(otherUser);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(10L, 1L))
                .isInstanceOf(ApiException.class);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("deleteNotification deletes only notifications owned by the user")
    void deleteNotification_ownedNotification_deletes() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(10L, 1L);

        verify(notificationRepository).delete(notification);
    }
}
