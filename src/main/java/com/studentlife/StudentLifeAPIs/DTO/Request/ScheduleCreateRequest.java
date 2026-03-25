package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequest {

    private String title;
    private String description;
    private int dayOfWeek;
    private Instant startTime;
    private Instant endTime;
    private String location;
}
