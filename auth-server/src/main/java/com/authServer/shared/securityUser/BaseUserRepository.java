package com.authServer.shared.securityUser;

import com.authServer.shared.defaultInterfaces.DefaultRepository;
import com.authServer.shared.models.baseUser.BaseUserEntity;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public interface BaseUserRepository extends DefaultRepository<BaseUserEntity, Long> {

    Optional<BaseUserEntity> findByUsername(String username);

}
