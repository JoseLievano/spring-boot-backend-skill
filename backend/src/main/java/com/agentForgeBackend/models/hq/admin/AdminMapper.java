package com.agentForgeBackend.models.hq.admin;

import com.agentForgeBackend.shared.defaultInterfaces.DefaultMapper;
import com.agentForgeBackend.shared.models.baseUser.UserRoles;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AdminMapper implements DefaultMapper<AdminDTO, AdminMiniDTO, AdminListDTO, AdminForm, AdminEntity> {

    @Override
    public AdminDTO toDTO(AdminEntity adminEntity) {
        return AdminDTO.builder()
                .firstName(adminEntity.getFirstName())
                .lastName(adminEntity.getLastName())
                .email(adminEntity.getEmail())
                .roles(adminEntity.getRoles().stream()
                        .map(UserRoles::getAuthority)
                        .collect(Collectors.toSet())
                )
                .build();
    }

    @Override
    public AdminMiniDTO toSmallDTO(AdminEntity adminEntity) {
        return AdminMiniDTO.builder()
                .firstName(adminEntity.getFirstName())
                .lastName(adminEntity.getLastName())
                .email(adminEntity.getEmail())
                .roles(adminEntity.getRoles().stream()
                        .map(UserRoles::getAuthority)
                        .collect(Collectors.toSet())
                )
                .build();
    }

    @Override
    public AdminListDTO toListDTO(AdminEntity adminEntity) {
        if (adminEntity == null) {
            return null;
        }

        return AdminListDTO.builder()
                .id(adminEntity.getId())
                .firstName(adminEntity.getFirstName())
                .lastName(adminEntity.getLastName())
                .email(adminEntity.getEmail())
                .username(adminEntity.getUsername())
                .roles(adminEntity.getRoles().stream()
                        .map(UserRoles::getAuthority)
                        .collect(Collectors.toSet())
                )
                .enabled(adminEntity.isEnabled())
                .dateCreated(adminEntity.getDateCreated())
                .lastLogin(adminEntity.getLastLogin())
                .build();
    }

    @Override
    public AdminEntity toEntity(AdminForm adminForm) {
        if (adminForm == null)
            return null;

        AdminEntity entity = new AdminEntity();
        if (adminForm.getFirstName() != null)
            entity.setFirstName(adminForm.getFirstName());
        if (adminForm.getLastName() != null)
            entity.setLastName(adminForm.getLastName());
        if (adminForm.getEmail() != null)
            entity.setEmail(adminForm.getEmail());
        if (adminForm.getUsername() != null)
            entity.setUsername(adminForm.getUsername());
        if (adminForm.getPassword() != null)
            entity.setPassword(adminForm.getPassword());
        if (!adminForm.getRoles().isEmpty()){
            entity.setRoles(
                    adminForm.getRoles().stream()
                            .map(UserRoles::valueOf)
                            .collect(Collectors.toSet())
            );
        }
        return entity;
    }
}
