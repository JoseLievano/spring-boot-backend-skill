package com.agentForgeBackend.models.hq.client;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.agentForgeBackend.models.hq.ListQueryTestDataFactory;
import com.agentForgeBackend.shared.query.FilterOperator;
import com.agentForgeBackend.shared.query.PageableRequest;
import com.agentForgeBackend.shared.query.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "task6-client-service", roles = "ADMIN")
class ClientServiceListQueryIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    private ClientEntity alpha;
    private ClientEntity beta;
    private ClientEntity gamma;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        clientRepository.flush();

        alpha = clientRepository.save(ListQueryTestDataFactory.client("client-service-alpha", true, ListQueryTestDataFactory.EARLY_DATE, 20_001L));
        beta = clientRepository.save(ListQueryTestDataFactory.client("client-service-beta", false, ListQueryTestDataFactory.MIDDLE_DATE, 20_002L));
        gamma = clientRepository.save(ListQueryTestDataFactory.client("client-service-gamma", true, ListQueryTestDataFactory.LATE_DATE, 20_003L));
        clientRepository.flush();
    }

    @Test
    void defaultRequestUsesProfileDefaultSortAndMapsClientListDtos() throws InvalidQueryRequestException {
        Page<ClientListDTO> page = clientService.getListPage(new PageableRequest());

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(ClientListDTO::getId)
                .containsExactly(alpha.getId(), beta.getId(), gamma.getId());
        assertThat(page.getContent().get(0).getUsername()).isEqualTo("client-service-alpha");
        assertThat(fieldNames(ClientListDTO.class)).doesNotContain("password", "apikey");
    }

    @Test
    void filtersSortsAndPaginatesClientListDtos() throws InvalidQueryRequestException {
        PageableRequest request = ListQueryTestDataFactory.request(
                0,
                1,
                List.of(ListQueryTestDataFactory.sort("username", SortDirection.DESC)),
                List.of(ListQueryTestDataFactory.filter("email", FilterOperator.CONTAINS, "example.com")));

        Page<ClientListDTO> page = clientService.getListPage(request);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(ClientListDTO::getUsername)
                .containsExactly("client-service-gamma");
    }

    @Test
    void rejectsSensitiveClientFilterFields() {
        PageableRequest apiKeyRequest = ListQueryTestDataFactory.request(
                0,
                20,
                List.of(),
                List.of(ListQueryTestDataFactory.filter("apikey", FilterOperator.EQUALS, 20_001L)));
        PageableRequest rolesRequest = ListQueryTestDataFactory.request(
                0,
                20,
                List.of(),
                List.of(ListQueryTestDataFactory.filter("roles", FilterOperator.EQUALS, "CLIENT")));

        assertThatThrownBy(() -> clientService.getListPage(apiKeyRequest))
                .isInstanceOf(InvalidQueryRequestException.class)
                .hasMessageContaining("apikey");
        assertThatThrownBy(() -> clientService.getListPage(rolesRequest))
                .isInstanceOf(InvalidQueryRequestException.class)
                .hasMessageContaining("roles");
    }

    private List<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .toList();
    }
}
