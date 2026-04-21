package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {

    @NotNull
    private Long assignmentId;

    @NotBlank
    private String content;
}
