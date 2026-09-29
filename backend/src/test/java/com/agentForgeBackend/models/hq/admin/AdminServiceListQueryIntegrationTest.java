package com.agentForgeBackend.models.hq.admin;

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
@WithMockUser(username = "task6-admin-service", roles = "ADMIN")
class AdminServiceListQueryIntegrationTest {

    @Autowired
    private AdminServiceImpl adminService;

    @Autowired
    private AdminRepository adminRepository;

    private AdminEntity alpha;
    private AdminEntity beta;
    private AdminEntity gamma;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();
        adminRepository.flush();

        alpha = adminRepository.save(ListQueryTestDataFactory.admin("admin-service-alpha", true, ListQueryTestDataFactory.EARLY_DATE));
        beta = adminRepository.save(ListQueryTestDataFactory.admin("admin-service-beta", false, ListQueryTestDataFactory.MIDDLE_DATE));
        gamma = adminRepository.save(ListQueryTestDataFactory.admin("admin-service-gamma", true, ListQueryTestDataFactory.LATE_DATE));
        adminRepository.flush();
    }

    @Test
    void defaultRequestUsesProfileDefaultSortAndMapsAdminListDtos() throws InvalidQueryRequestException {
        Page<AdminListDTO> page = adminService.getListPage(new PageableRequest());

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(AdminListDTO::getId)
                .containsExactly(alpha.getId(), beta.getId(), gamma.getId());
        assertThat(page.getContent().get(0).getUsername()).isEqualTo("admin-service-alpha");
        assertThat(fieldNames(AdminListDTO.class)).doesNotContain("password", "apikey");
    }

    @Test
    void filtersSortsAndPaginatesAdminListDtos() throws InvalidQueryRequestException {
        PageableRequest request = ListQueryTestDataFactory.request(
                0,
                1,
                List.of(ListQueryTestDataFactory.sort("username", SortDirection.DESC)),
                List.of(ListQueryTestDataFactory.filter("email", FilterOperator.CONTAINS, "example.com")));

        Page<AdminListDTO> page = adminService.getListPage(request);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(AdminListDTO::getUsername)
                .containsExactly("admin-service-gamma");
    }

    @Test
    void rejectsPasswordAsAdminFilterField() {
        PageableRequest request = ListQueryTestDataFactory.request(
                0,
                20,
                List.of(),
                List.of(ListQueryTestDataFactory.filter("password", FilterOperator.EQUALS, "secret")));

        assertThatThrownBy(() -> adminService.getListPage(request))
                .isInstanceOf(InvalidQueryRequestException.class)
                .hasMessageContaining("password");
    }

    private List<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .toList();
    }
}
