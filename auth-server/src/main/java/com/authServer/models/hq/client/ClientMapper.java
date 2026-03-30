package com.authServer.models.hq.client;

import com.authServer.shared.defaultInterfaces.DefaultMapper;
import com.authServer.shared.models.baseUser.UserRoles;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ClientMapper implements DefaultMapper<ClientDTO, ClientMiniDTO, ClientForm, ClientEntity> {

    @Lazy
    public ClientMapper( ){
    }

    @Override
    public ClientDTO toDTO(ClientEntity clientEntity) {
        if (clientEntity == null)
            return null;
        return ClientDTO.builder()
                .id(clientEntity.getId())
                .firstName(clientEntity.getFirstName())
                .lastName(clientEntity.getLastName())
                .email(clientEntity.getEmail())
                .username(clientEntity.getUsername())
                .roles(clientEntity.getRoles().stream()
                        .map(UserRoles::getAuthority)
                        .collect(Collectors.toSet())
                )
                .build();
    }

    @Override
    public ClientMiniDTO toSmallDTO(ClientEntity clientEntity) {
        if (clientEntity == null)
            return null;

        return ClientMiniDTO.builder()
                .firstName(clientEntity.getFirstName())
                .lastName(clientEntity.getLastName())
                .email(clientEntity.getEmail())
                .username(clientEntity.getUsername())
                .build();
    }

    @Override
    public ClientEntity toEntity(ClientForm clientForm) {
        if (clientForm == null)
            return null;

        ClientEntity entity = new ClientEntity();
        if (clientForm.getFirstName() != null)
            entity.setFirstName(clientForm.getFirstName());
        if (clientForm.getLastName() != null)
            entity.setLastName(clientForm.getLastName());
        if (clientForm.getUsername() != null)
            entity.setUsername(clientForm.getUsername());
        if (clientForm.getEmail() != null)
            entity.setEmail(clientForm.getEmail());
        return entity;
    }
}
