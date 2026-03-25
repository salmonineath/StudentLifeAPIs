package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.ScheduleMapper;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.validation;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final AuthUtil authUtil;
    private final ScheduleMapper scheduleMapper;

    @Override
    public ApiResponse<?> createSchedule(ScheduleCreateRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw validation("Start time must be before end time.");
        }

        Long userId = authUtil.getAuthenticatedUser().getId();

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> notFound("User not found."));

        Schedules schedule = scheduleMapper.toScheduleEntityCreate(request);
        schedule.setUser(user);

        scheduleRepository.save(schedule);

        ScheduleResponse scheduleResponse = scheduleMapper.toResponse(schedule);

        return new ApiResponse<>(
                201,
                true,
                "Create Schedule successfully.",
                scheduleResponse
        );
    }
}
