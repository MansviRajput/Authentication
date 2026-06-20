package com.mansvi.auth_backend.services.Impl;

import com.mansvi.auth_backend.dtos.request.CreateUserRequest;
import com.mansvi.auth_backend.dtos.request.UpdateUserRequest;
import com.mansvi.auth_backend.dtos.response.UserResponse;
import com.mansvi.auth_backend.dtos.response.UserSummaryResponse;
import com.mansvi.auth_backend.entities.User;
import com.mansvi.auth_backend.exceptions.EmailAlreadyExistException;
import com.mansvi.auth_backend.exceptions.UserNotFoundException;
import com.mansvi.auth_backend.repositories.UserRepository;
import com.mansvi.auth_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistException("Email already exists" + request.getEmail());
        }

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setImage(generateGravatarUrl(request.getEmail()));
        user.setEnable(true);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id " + id
                ));
        return modelMapper.map(user, UserResponse.class);
    }

    //for image generate the url
    private String generateGravatarUrl(String email) {
        String hash = DigestUtils.md5DigestAsHex(email.trim().toLowerCase().getBytes());
        return "https://www.gravatar.com/avatar/" + hash + "?d=identicon";
    }
}
