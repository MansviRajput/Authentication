package com.mansvi.auth_backend.config;

import com.mansvi.auth_backend.dtos.request.CreateUserRequest;
import com.mansvi.auth_backend.dtos.response.RoleResponse;
import com.mansvi.auth_backend.dtos.response.UserResponse;
import com.mansvi.auth_backend.dtos.response.UserSummaryResponse;
import com.mansvi.auth_backend.entities.Role;
import com.mansvi.auth_backend.entities.RoleType;
import com.mansvi.auth_backend.entities.User;
import com.mansvi.auth_backend.repositories.RoleRepository;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class ProjectConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            for (RoleType type : RoleType.values()) {
                if (roleRepository.findByRoleName(type).isEmpty()) {
                    roleRepository.save(Role.builder().roleName(type).build());
                }
            }
        };
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(CreateUserRequest.class, User.class)
                .addMappings(mapper ->
                        {mapper.skip(User::setPassword);

                        mapper.skip(User::setImage);

                        mapper.skip(User::setRoles);
                        });

        Converter<Set<Role>, Set<RoleResponse>> rolesToRoleResponse =
                ctx -> ctx.getSource() == null ? null :
                                                        ctx.getSource().stream()
                                                                .map(role -> {
                                                                    RoleResponse r= new RoleResponse();
                                                                    r.setId(role.getId());
                                                                    r.setRoleName(role.getRoleName());
                                                                    return r;
                                                                })
                                                                .collect(Collectors.toSet());
        modelMapper.createTypeMap(User.class, UserResponse.class)
                .addMappings(mapper ->{
                    mapper.using(rolesToRoleResponse)
                            .map(User::getRoles, UserResponse::setRoles);
                });

        modelMapper.createTypeMap(User.class, UserSummaryResponse.class);


        return modelMapper;
    }
}
