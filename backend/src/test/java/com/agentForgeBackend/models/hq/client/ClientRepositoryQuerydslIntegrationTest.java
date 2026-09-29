package com.agentForgeBackend.models.hq.client;

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
class ClientRepositoryQuerydslIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void findAllWithBooleanPredicatePageableAndSortReturnsExpectedClientPage() {
        persistClients();

        Page<ClientEntity> page = clientRepository.findAll(
                QClientEntity.clientEntity.enabled.isTrue(),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "username")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getContent())
                .extracting(ClientEntity::getUsername)
                .containsExactly("client-gamma");
    }

    @Test
    void findAllWithStringPredicateReturnsMatchingClient() {
        persistClients();

        Page<ClientEntity> page = clientRepository.findAll(
                QClientEntity.clientEntity.username.containsIgnoreCase("ALPHA"),
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent())
                .extracting(ClientEntity::getUsername)
                .containsExactly("client-alpha");
    }

    @Test
    void findAllWithIdAndDatePredicatesReturnsExpectedClients() {
        List<ClientEntity> clients = persistClients();
        QClientEntity client = QClientEntity.clientEntity;
        Predicate predicate = client.id.goe(clients.get(1).getId())
                .and(client.dateCreated.goe(Date.from(ListQueryTestDataFactory.MIDDLE_DATE)));

        Page<ClientEntity> page = clientRepository.findAll(
                predicate,
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(ClientEntity::getUsername)
                .containsExactly("client-beta", "client-gamma");
    }

    private List<ClientEntity> persistClients() {
        ClientEntity alpha = ListQueryTestDataFactory.client("client-alpha", true, ListQueryTestDataFactory.EARLY_DATE, 10_001L);
        ClientEntity beta = ListQueryTestDataFactory.client("client-beta", false, ListQueryTestDataFactory.MIDDLE_DATE, 10_002L);
        ClientEntity gamma = ListQueryTestDataFactory.client("client-gamma", true, ListQueryTestDataFactory.LATE_DATE, 10_003L);

        entityManager.persist(alpha);
        entityManager.persist(beta);
        entityManager.persist(gamma);
        entityManager.flush();
        entityManager.clear();

        return List.of(alpha, beta, gamma);
    }
}
