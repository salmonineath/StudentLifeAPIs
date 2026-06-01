package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedules, Long>,
        JpaSpecificationExecutor<Schedules> {

    boolean existsByUser(Users user);

    boolean existsByTitleAndUserId(String title, Long userId);

    void deleteByAssignmentId(Long assignmentId);

    @Query(value = "SELECT * FROM schedules WHERE user_id = :userId AND type = 'ONE_TIME' AND CAST(start_time AS DATE) = :today", nativeQuery = true)
    List<Schedules> findOneTimeByUserAndDate(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query(value = "SELECT * FROM schedules WHERE user_id = :userId AND type = 'RECURRING' AND day_of_week = :dayOfWeek", nativeQuery = true)
    List<Schedules> findRecurringByUserAndDayOfWeek(@Param("userId") Long userId, @Param("dayOfWeek") int dayOfWeek);
}