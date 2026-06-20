package com.mansvi.auth_backend.dtos.request;

import com.mansvi.auth_backend.entities.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignRoleRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private RoleType roleName;
}
