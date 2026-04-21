package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long assignmentId;

    private String assignmentTitle;
    private String subject;
    private String ownerName;
    private String ownerUsername;

    private int memberCount;

    private String lastMessage;
    private String lastMessageTime;
    private String lastMessageSender;

}
