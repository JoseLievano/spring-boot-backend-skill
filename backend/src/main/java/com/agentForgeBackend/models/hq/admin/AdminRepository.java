package com.agentForgeBackend.models.hq.admin;

import com.agentForgeBackend.shared.defaultInterfaces.DefaultRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends DefaultRepository <AdminEntity, Long> {

    AdminEntity findByUsername(String username);

    AdminEntity findByEmail(String email);
}
