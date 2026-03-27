package com.studentlife.StudentLifeAPIs.Entity;

import com.studentlife.StudentLifeAPIs.Enum.ReminderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "assignment_reminder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignments assignments;

    @Column(name = "remind_at", nullable = false)
    private Date remindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType type;

    @Builder.Default
    @Column(name = "is_sent", nullable = false)
    private Boolean IsSent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
