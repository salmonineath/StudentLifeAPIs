package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Response.GroupMessageResponse;
import com.studentlife.StudentLifeAPIs.Entity.GroupMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface GroupMessageMapper {

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.fullname", target = "senderFullname")
    @Mapping(source = "sender.username", target = "senderUsername")
    GroupMessageResponse toResponse(GroupMessage message);

    List<GroupMessageResponse> toResponseList(List<GroupMessage> messages);
}
