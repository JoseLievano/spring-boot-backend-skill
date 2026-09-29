package com.agentForgeBackend.models.hq.admin;

import com.agentForgeBackend.models.hq.ListQueryTestDataFactory;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("repository")
class AdminRepositoryQuerydslIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void findAllWithBooleanPredicatePageableAndSortReturnsExpectedAdminPage() {
        persistAdmins();

        Page<AdminEntity> page = adminRepository.findAll(
                QAdminEntity.adminEntity.enabled.isTrue(),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "username")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getContent())
                .extracting(AdminEntity::getUsername)
                .containsExactly("admin-gamma");
    }

    @Test
    void findAllWithStringPredicateReturnsMatchingAdmin() {
        persistAdmins();

        Page<AdminEntity> page = adminRepository.findAll(
                QAdminEntity.adminEntity.username.containsIgnoreCase("ALPHA"),
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent())
                .extracting(AdminEntity::getUsername)
                .containsExactly("admin-alpha");
    }

    @Test
    void findAllWithIdAndDatePredicatesReturnsExpectedAdmins() {
        List<AdminEntity> admins = persistAdmins();
        QAdminEntity admin = QAdminEntity.adminEntity;
        Predicate predicate = admin.id.goe(admins.get(1).getId())
                .and(admin.dateCreated.goe(Date.from(ListQueryTestDataFactory.MIDDLE_DATE)));

        Page<AdminEntity> page = adminRepository.findAll(
                predicate,
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(AdminEntity::getUsername)
                .containsExactly("admin-beta", "admin-gamma");
    }

    private List<AdminEntity> persistAdmins() {
        AdminEntity alpha = ListQueryTestDataFactory.admin("admin-alpha", true, ListQueryTestDataFactory.EARLY_DATE);
        AdminEntity beta = ListQueryTestDataFactory.admin("admin-beta", false, ListQueryTestDataFactory.MIDDLE_DATE);
        AdminEntity gamma = ListQueryTestDataFactory.admin("admin-gamma", true, ListQueryTestDataFactory.LATE_DATE);

        entityManager.persist(alpha);
        entityManager.persist(beta);
        entityManager.persist(gamma);
        entityManager.flush();
        entityManager.clear();

        return List.of(alpha, beta, gamma);
    }
}
