package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Data;

import java.time.Instant;

@Data
public class NoteResponse {

    private Long id;
    private String title;
    private String content;
    private NoteCategoryResponse category;
    private Instant createdAt;
    private Instant updatedAt;
}
