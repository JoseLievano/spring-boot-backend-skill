package com.authServer.models.hq.client;

import com.authServer.shared.models.baseUser.BaseUserEntity;
import com.authServer.shared.models.baseUser.UserRoles;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Table (name = "client")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ClientEntity extends BaseUserEntity {

    @Column(name = "apikey", unique = true)
    private Long apikey;

    public ClientEntity(
            String firstName,
            String lastName,
            String email,
            Set<UserRoles> roles,
            String username,
            String password
    ){
        super(firstName, lastName, email, roles, username, password);
    }

    public BaseUserEntity getBaseUser() {
        return this.getBaseUser();
    }
}
