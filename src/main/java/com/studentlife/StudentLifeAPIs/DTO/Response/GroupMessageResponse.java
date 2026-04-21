package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageResponse {
    private Long id;
    private Long assignmentId;
    private Long senderId;

    private String senderFullname;
    private String senderUsername;
    private String content;
    private Instant createdAt;
}
