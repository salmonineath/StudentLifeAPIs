package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Data;

import java.time.Instant;

@Data
public class NoteCategoryResponse {

    private Long id;
    private String name;
    private String color;

    private Instant createdAt;
    private Instant updatedAt;
}
