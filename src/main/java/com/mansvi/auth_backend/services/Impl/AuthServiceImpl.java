package com.mansvi.auth_backend.services.Impl;

import com.mansvi.auth_backend.dtos.request.*;
import com.mansvi.auth_backend.dtos.response.AuthResponse;
import com.mansvi.auth_backend.dtos.response.UserResponse;
import com.mansvi.auth_backend.entities.User;
import com.mansvi.auth_backend.exceptions.*;
import com.mansvi.auth_backend.repositories.UserRepository;
import com.mansvi.auth_backend.security.CustomUserDetail;
import com.mansvi.auth_backend.security.CustomUserDetail;
import com.mansvi.auth_backend.security.JwtService;
import com.mansvi.auth_backend.services.AuthService;
import com.mansvi.auth_backend.services.UserService;
import com.mansvi.auth_backend.Utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserResponse signUp(CreateUserRequest request) {
        UserResponse created = userService.createUser(request);

        User user = userRepository.findById(created.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found after creation"));

        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // TODO: send verification email containing user.getVerificationToken()

        return created;
    }

    @Override
    @Transactional
    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getVerificationTokenExpires().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpires(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!user.isEmailVerified()) {
            throw new AccountNotVerifiedException("Please verify your email before logging in");
        }

        return issueTokens(userDetails, response);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.readCookie(request, "refreshToken")
                .orElseThrow(() -> new InvalidTokenException("Missing refresh token"));

        if (!jwtService.isTokenValid(refreshToken) || !"refresh".equals(jwtService.extractTokenType(refreshToken))) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        return issueTokens(new CustomUserDetail(user), response);
    }

    @Override
    @Transactional
    public void logout(UUID userId, HttpServletResponse response) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpires(null);
            userRepository.save(user);
        });

        CookieUtil.clearCookie(response, "accessToken", "/");
        CookieUtil.clearCookie(response, "refreshToken", "/api/v1/auth");
    }

    private AuthResponse issueTokens(CustomUserDetail userDetails, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        User user = userDetails.getUser();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpires(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiryMs() / 1000));
        userRepository.save(user);

        CookieUtil.addCookie(response, "accessToken", accessToken, "/", jwtService.getAccessTokenExpiryMs() / 1000);
        CookieUtil.addCookie(response, "refreshToken", refreshToken, "/api/v1/auth", jwtService.getRefreshTokenExpiryMs() / 1000);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setExpiresIn(jwtService.getAccessTokenExpiryMs() / 1000);
        authResponse.setUser(modelMapper.map(user, UserResponse.class));
        return authResponse;
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            user.setResetPasswordToken(UUID.randomUUID().toString());
            user.setResetPasswordTokenExpires(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            // TODO: send email containing user.getResetPasswordToken()
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (user.getResetPasswordTokenExpires().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpires(null);
        userRepository.save(user);
    }

    @Override
    public AuthResponse issueTokensForUser(User user, HttpServletResponse response) {
        return issueTokens(new CustomUserDetail(user), response);
    }
}