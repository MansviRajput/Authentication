package com.mansvi.auth_backend.services;

import com.mansvi.auth_backend.dtos.request.*;
import com.mansvi.auth_backend.dtos.response.AuthResponse;
import com.mansvi.auth_backend.dtos.response.UserResponse;
import com.mansvi.auth_backend.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface AuthService {

    UserResponse signUp(CreateUserRequest createUserRequest);
    void verifyUser(String token);
    AuthResponse login(LoginRequest loginRequest, HttpServletResponse response);
    AuthResponse refreshToken(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse);
    void logout(UUID userId, HttpServletResponse response);
    void changePassword(UUID userId,ChangePasswordRequest changePasswordRequest);
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
    AuthResponse issueTokensForUser(User user, HttpServletResponse response);
}
