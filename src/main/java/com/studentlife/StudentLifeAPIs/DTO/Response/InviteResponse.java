package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InviteResponse {

    private String email;
    private String inviteLink;
}
