package com.authServer.shared.defaultInterfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DefaultRepository <ENTITY, ID> extends JpaRepository<ENTITY, ID> {
}