package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;

    // Optional deep-linking fields. `referenceId` is the id of the referenced entity;
    // `link` is an explicit relative in-app path (must start with "/", never an absolute URL).
    private Long referenceId;
    private String link;
}
