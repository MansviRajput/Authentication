package com.mansvi.auth_backend.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansvi.auth_backend.dtos.response.AuthResponse;
import com.mansvi.auth_backend.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

        AuthResponse authResponse = authService.issueTokensForUser(principal.getUser(), response);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));

        // Frontend banya pachi, upar nu JSON write hatavi ne aa line use karo:
        // response.sendRedirect("http://localhost:3000/oauth2/redirect");
    }
}