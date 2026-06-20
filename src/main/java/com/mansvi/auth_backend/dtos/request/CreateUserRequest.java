package com.mansvi.auth_backend.dtos.request;

import com.mansvi.auth_backend.entities.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "username is required")
    @Size(min = 6, max = 30)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email formate")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "password must be at least 8 character")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain uppercase, lowercase and a number"
    )
    private String password;

    private Provider provider = Provider.LOCAL;
}
