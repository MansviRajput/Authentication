package com.mansvi.auth_backend.services;


import com.mansvi.auth_backend.dtos.request.CreateUserRequest;
import com.mansvi.auth_backend.dtos.request.UpdateUserRequest;
import com.mansvi.auth_backend.dtos.response.UserResponse;
import com.mansvi.auth_backend.dtos.response.UserSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

}
