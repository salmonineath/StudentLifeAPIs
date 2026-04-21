package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.ChatMessageRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupMessageResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupResponse;
import com.studentlife.StudentLifeAPIs.Service.GroupChatService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final AuthUtil authUtil;

    /**
     * WebSocket — send a message
     * Frontend sends to: /app/chat.send
     * Backend broadcasts to: /topic/group/{assignmentId}
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long senderId = authUtil.getUserIdFromPrincipal(principal);
        groupChatService.sendMessage(request, senderId);
    }

    /**
     * REST — GET /api/v1/chat/groups
     * Returns all groups the current user belongs to
     */
    @GetMapping("/api/v1/chat/groups")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMyGroups() {
        return ResponseEntity.ok(groupChatService.getMyGroups());
    }


    /**
     * REST — GET /api/v1/chat/{assignmentId}/history
     * Load chat history when opening the group page
     */
    @GetMapping("/api/v1/chat/{assignmentId}/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<GroupMessageResponse>>> getHistory(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(groupChatService.getChatHistory(assignmentId));
    }

    /**
     * REST — DELETE /api/v1/chat/{assignmentId}/history
     * Clear chat history (owner only)
     */
    @DeleteMapping("/api/v1/chat/{assignmentId}/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> clearHistory(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(groupChatService.clearChatHistory(assignmentId));
    }
}