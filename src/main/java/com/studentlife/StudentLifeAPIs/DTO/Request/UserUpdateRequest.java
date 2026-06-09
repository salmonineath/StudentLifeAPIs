package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullname;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 120, message = "University must be at most 120 characters")
    private String university;

    @Size(max = 120, message = "Major must be at most 120 characters")
    private String major;

    @Size(max = 50, message = "Academic year must be at most 50 characters")
    private String academic_year;
}
