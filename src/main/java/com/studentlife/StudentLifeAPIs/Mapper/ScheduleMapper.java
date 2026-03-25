package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface ScheduleMapper {

    Schedules toScheduleEntityCreate(ScheduleCreateRequest request);

    @Mapping(source = "user.id", target = "createdBy.id")
    @Mapping(source = "user.username", target = "createdBy.username")
    ScheduleResponse toResponse(Schedules schedule);
}