package com.agentForgeBackend.shared.defaultInterfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DefaultRepository <ENTITY, ID> extends JpaRepository<ENTITY, ID>, QuerydslPredicateExecutor<ENTITY> {
}
