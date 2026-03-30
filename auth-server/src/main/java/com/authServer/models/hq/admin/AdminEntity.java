package com.authServer.models.hq.admin;

import com.authServer.shared.models.baseUser.BaseUserEntity;
import com.authServer.shared.models.baseUser.UserRoles;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Set;

@Table (name = "admin")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class AdminEntity extends BaseUserEntity {

    public AdminEntity(
        String firstName,
        String lastName,
        String email,
        Set<UserRoles> roles,
        String username,
        String password
    )
    {
        super(firstName, lastName, email, roles, username, password);
    }
}
