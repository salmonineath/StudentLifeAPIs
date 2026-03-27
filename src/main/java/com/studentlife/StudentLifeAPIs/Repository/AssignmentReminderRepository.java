package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.AssignmentReminder;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AssignmentReminderRepository extends JpaRepository<AssignmentReminder, Long> {
    List<AssignmentReminder> findAllByIsSentFalseAndRemindAtLessThenEqual(Date now);

    List<AssignmentReminder> findAllByAssignment(Assignments assignment);
}
