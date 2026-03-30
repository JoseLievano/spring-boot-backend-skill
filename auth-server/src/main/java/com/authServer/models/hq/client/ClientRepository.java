package com.authServer.models.hq.client;

import com.authServer.shared.defaultInterfaces.DefaultRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends DefaultRepository <ClientEntity, Long> {

    Optional<ClientEntity> findByUsername(String username);

    Optional<ClientEntity> findByEmail(String email);
}
