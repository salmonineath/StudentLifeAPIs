package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRequest {

    @NotBlank
    private String token;
}
