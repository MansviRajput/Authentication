package com.mansvi.auth_backend.security.oauth2;

import com.mansvi.auth_backend.entities.Provider;
import com.mansvi.auth_backend.entities.Role;
import com.mansvi.auth_backend.entities.RoleType;
import com.mansvi.auth_backend.entities.User;
import com.mansvi.auth_backend.repositories.RoleRepository;
import com.mansvi.auth_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); // Google/GitHub thi raw data mangavyu

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" / "github"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        String email = userInfo.getEmail();

        // GitHub email private hoy to null aave - ek extra API call thi fetch kariye
        if ((email == null || email.isBlank()) && "github".equalsIgnoreCase(registrationId)) {
            email = fetchGitHubPrimaryEmail(userRequest);
        }

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email permission not granted by " + registrationId);
        }

        String finalEmail = email;
        User user = userRepository.findByEmail(finalEmail)
                .map(existing -> syncUser(existing, userInfo))
                .orElseGet(() -> registerNewUser(registrationId, userInfo, finalEmail));

        return new CustomOAuth2User(user, attributes);
    }

    private User registerNewUser(String registrationId, OAuth2UserInfo userInfo, String email) {
        Role userRole = roleRepository.findByRoleName(RoleType.USER)
                .orElseThrow(() -> new IllegalStateException("USER role not seeded — check CommandLineRunner"));

        User user = User.builder()
                .username(generateUniqueUsername(userInfo, email))
                .email(email)
                .password(null)                       // OAuth user ne password nathi
                .image(userInfo.getImageUrl())
                .enable(true)
                .provider(Provider.valueOf(registrationId.toUpperCase()))
                .emailVerified(true)                  // Google/GitHub e already verify kari didhu
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        return userRepository.save(user);
    }

    private User syncUser(User existing, OAuth2UserInfo userInfo) {
        if (userInfo.getImageUrl() != null) {
            existing.setImage(userInfo.getImageUrl());
        }
        return userRepository.save(existing);
    }

    private String generateUniqueUsername(OAuth2UserInfo userInfo, String email) {
        String base = (userInfo.getName() != null ? userInfo.getName() : email.split("@")[0])
                .replaceAll("\\s+", "").toLowerCase();
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private String fetchGitHubPrimaryEmail(OAuth2UserRequest userRequest) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
            headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");

            ResponseEntity<List> response = restTemplate.exchange(
                    "https://api.github.com/user/emails", HttpMethod.GET,
                    new HttpEntity<>(headers), List.class);

            List<Map<String, Object>> emails = response.getBody();
            if (emails != null) {
                for (Map<String, Object> e : emails) {
                    if (Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified"))) {
                        return (String) e.get("email");
                    }
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }
}