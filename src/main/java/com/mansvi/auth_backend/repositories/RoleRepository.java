package com.mansvi.auth_backend.repositories;

import com.mansvi.auth_backend.entities.Role;
import com.mansvi.auth_backend.entities.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleName(RoleType roleName);
}
