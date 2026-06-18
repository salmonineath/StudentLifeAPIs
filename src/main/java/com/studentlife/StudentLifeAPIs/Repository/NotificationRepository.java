package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);

    // Paginated variants for the list endpoints
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Non-paged unread set — still needed by markAllAsRead to mark every unread row
    List<Notification> findByRecipientIdAndIsReadFalse(Long userId);

    long countByRecipientIdAndIsReadFalse(Long userId);
}
