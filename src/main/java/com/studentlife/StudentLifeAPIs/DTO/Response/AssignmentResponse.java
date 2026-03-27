package com.studentlife.StudentLifeAPIs.DTO.Response;

import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Enum.Priority;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private Priority priority;
    private AssignmentStatus status;
    private Date dueDate;
    private Instant createdAt;
    private List<ReminderResponse> reminders;
}