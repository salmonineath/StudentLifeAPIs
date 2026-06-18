package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/my-schedule")
    public ResponseEntity<ApiResponse<?>> getMySchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(scheduleService.getMySchedules(startDate, endDate));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getById(scheduleId));
    }

    @PostMapping("/one-time")
    public ResponseEntity<ApiResponse<?>> createOneTime(
            @RequestBody @Valid OneTimeScheduleRequest request
    ) {
        return ResponseEntity.status(201).body(scheduleService.createOneTime(request));
    }

    @PostMapping("/recurring")
    public ResponseEntity<ApiResponse<?>> createRecurring(
            @RequestBody @Valid RecurringScheduleRequest request
    ) {
        return ResponseEntity.status(201).body(scheduleService.createRecurring(request));
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<?>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody @Valid ScheduleUpdateRequest request
    ) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, request));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<?>> deleteSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.deleteSchedule(scheduleId));
    }
}
