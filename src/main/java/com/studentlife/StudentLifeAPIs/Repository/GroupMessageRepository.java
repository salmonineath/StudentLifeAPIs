package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    // Get all messages for a group, oldest first
    List<GroupMessage> findByAssignmentIdOrderByCreatedAtAsc(Long assignmentId);

    // Paginated history for a group (order supplied via Pageable sort)
    Page<GroupMessage> findByAssignmentId(Long assignmentId, Pageable pageable);

    // Get only the last message for a group (for preview)
    java.util.Optional<GroupMessage> findTopByAssignmentIdOrderByCreatedAtDesc(Long assignmentId);

    // Delete all messages for a group (manual clear by student)
    void deleteByAssignmentId(Long assignmentId);

    // Auto-delete messages older than 5 days (scheduler)
    @Modifying
    @Query("DELETE FROM GroupMessage m WHERE m.createdAt < :cutoff")
    void deleteOlderThan(@Param("cutoff") Instant cutoff);

    List<GroupMessage> findByAssignmentIdInOrderByCreatedAtDesc(List<Long> assignmentIds, Pageable pageable);
}