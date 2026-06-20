package com.mansvi.auth_backend.dtos.response;

import com.mansvi.auth_backend.entities.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    private UUID id;
    private RoleType roleName;
}
