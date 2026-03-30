package com.authServer.models.hq.admin;

import com.authServer.shared.defaultInterfaces.DefaultRepository;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends DefaultRepository <AdminEntity, Long> {

    AdminEntity findByUsername(String username);

    AdminEntity findByEmail(String email);
}
