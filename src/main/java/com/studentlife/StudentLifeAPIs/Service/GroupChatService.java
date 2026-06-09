package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.ChatMessageRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupMessageResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.MemberResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;

import java.util.List;

public interface GroupChatService {

    ApiResponse<List<GroupResponse>> getMyGroups();

    GroupMessageResponse sendMessage(ChatMessageRequest request, Long senderId);

    ApiResponse<PaginatedResponse<GroupMessageResponse>> getChatHistory(Long assignmentId, int page, int size);

    ApiResponse<?> clearChatHistory(Long assignmentId);

    ApiResponse<List<MemberResponse>> getGroupMember(Long assignmentId);

    void userJoined(Long assignmentId, Long userId, String username);

    void userLeft(Long assignmentId, Long userId, String username);
}
