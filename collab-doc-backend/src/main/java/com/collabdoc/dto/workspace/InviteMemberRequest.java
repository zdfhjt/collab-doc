package com.collabdoc.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteMemberRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
}
