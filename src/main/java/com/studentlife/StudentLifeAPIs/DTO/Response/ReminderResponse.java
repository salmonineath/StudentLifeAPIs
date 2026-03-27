package com.studentlife.StudentLifeAPIs.DTO.Response;

import com.studentlife.StudentLifeAPIs.Enum.ReminderType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ReminderResponse {
    private Long id;
    private Date remindAt;
    private ReminderType type;
    private Boolean isSent;
}