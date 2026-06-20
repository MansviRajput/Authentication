package com.mansvi.auth_backend.controllers;

import com.mansvi.auth_backend.dtos.request.*;
import com.mansvi.auth_backend.dtos.response.AuthResponse;
import com.mansvi.auth_backend.dtos.response.MessageResponse;
import com.mansvi.auth_backend.dtos.response.UserResponse;
import com.mansvi.auth_backend.security.CustomUserDetail;
import com.mansvi.auth_backend.services.AuthService;
import com.mansvi.auth_backend.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyUser(@RequestParam String token) {
        authService.verifyUser(token);
        return ResponseEntity.ok(new MessageResponse("Account verified successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal CustomUserDetail userDetails,
                                                  HttpServletResponse response) {
        authService.logout(userDetails.getUserId(), response);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal CustomUserDetail userDetails,
                                                          @RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new MessageResponse("If that email is registered, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomUserDetail userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getUserId()));
    }
}