package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Data;

import java.time.Instant;

@Data
public class ScheduleResponse {

    private Long id;
    private String title;
    private String description;
    private int dayOfWeek;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private UserSummaryResponse createdBy;
}
