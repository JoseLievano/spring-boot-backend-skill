package com.agentForgeBackend.shared.securityUser;

import com.agentForgeBackend.shared.defaultInterfaces.DefaultRepository;
import com.agentForgeBackend.shared.models.baseUser.BaseUserEntity;

import java.util.Optional;

public interface BaseUserRepository extends DefaultRepository<BaseUserEntity, Long> {

    Optional<BaseUserEntity> findByUsername(String username);

}
