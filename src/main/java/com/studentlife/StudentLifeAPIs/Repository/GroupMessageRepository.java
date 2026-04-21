package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    // Get all messages for a group, oldest first
    List<GroupMessage> findByAssignmentIdOrderByCreatedAtAsc(Long assignmentId);

    // Delete all messages for a group (manual clear by student)
    void deleteByAssignmentId(Long assignmentId);

    // Auto-delete messages older than 5 days (scheduler)
    @Modifying
    @Query("DELETE FROM GroupMessage m WHERE m.createdAt < :cutoff")
    void deleteOlderThan(@Param("cutoff") Instant cutoff);
}