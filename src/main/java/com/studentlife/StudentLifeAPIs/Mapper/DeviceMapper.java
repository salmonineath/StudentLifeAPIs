package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterDeviceRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.UserDevices;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface DeviceMapper {

    // ============
    // RESPONSES
    // ============
    UserDeviceResponse toResponse(UserDevices device);

    List<UserDeviceResponse> toResponseList(List<UserDevices> devices);

    // ============
    // REQUEST -> ENTITY (new device)
    // ============
    // Anything the service must set itself (id, user, timestamps, ip, active flag)
    // is ignored here so MapStruct never silently overwrites them.
    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "user",          ignore = true)
    @Mapping(target = "ipAddress",     ignore = true)
    @Mapping(target = "firstSeenAt",   ignore = true)
    @Mapping(target = "lastSeenAt",    ignore = true)
    @Mapping(target = "active",        ignore = true)
    UserDevices toEntity(RegisterDeviceRequest request);

    // ============
    // REQUEST -> EXISTING ENTITY (update only)
    // ============
    // Used when a known device logs back in. Only copies non-null fields,
    // and never touches identity / ownership / firstSeenAt.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "user",          ignore = true)
    @Mapping(target = "deviceId",      ignore = true) // identity of the device, must not change
    @Mapping(target = "firstSeenAt",   ignore = true) // never reset
    @Mapping(target = "ipAddress",     ignore = true) // service sets from HttpServletRequest
    @Mapping(target = "lastSeenAt",    ignore = true) // service sets to Instant.now()
    @Mapping(target = "active",        ignore = true) // service controls lifecycle
    void updateEntity(RegisterDeviceRequest request, @MappingTarget UserDevices entity);
}
