package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.Data;

@Data
public class NotificationRequest {
    private String title;
    private String message;

    // Optional deep-linking fields. `referenceId` is the id of the referenced entity;
    // `link` is an explicit relative in-app path (must start with "/", never an absolute URL).
    private Long referenceId;
    private String link;
}
