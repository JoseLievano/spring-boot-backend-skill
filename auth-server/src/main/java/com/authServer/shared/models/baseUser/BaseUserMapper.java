package com.authServer.shared.models.baseUser;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BaseUserMapper {

    public BaseUserDTO toDTO(BaseUserEntity user){
        return BaseUserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(UserRoles::getAuthority)
                        .collect(Collectors.toSet())
                )
                .build();
    }

    public BaseUserMiniDTO toMiniDTO(BaseUserEntity user){
        return BaseUserMiniDTO.builder()
                .username(user.getUsername())
                .build();
    }

}
