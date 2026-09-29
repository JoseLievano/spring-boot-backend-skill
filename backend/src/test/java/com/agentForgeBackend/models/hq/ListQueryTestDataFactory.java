package com.agentForgeBackend.models.hq;

import com.agentForgeBackend.models.hq.admin.AdminEntity;
import com.agentForgeBackend.models.hq.client.ClientEntity;
import com.agentForgeBackend.shared.models.baseUser.UserRoles;
import com.agentForgeBackend.shared.query.FilterOperationRequest;
import com.agentForgeBackend.shared.query.FilterOperator;
import com.agentForgeBackend.shared.query.FilterRequest;
import com.agentForgeBackend.shared.query.PageableRequest;
import com.agentForgeBackend.shared.query.SortDirection;
import com.agentForgeBackend.shared.query.SortRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;

public final class ListQueryTestDataFactory {

    public static final Instant EARLY_DATE = Instant.parse("2024-01-01T00:00:00Z");
    public static final Instant MIDDLE_DATE = Instant.parse("2024-01-02T00:00:00Z");
    public static final Instant LATE_DATE = Instant.parse("2024-01-03T00:00:00Z");

    private ListQueryTestDataFactory() {
    }

    public static AdminEntity admin(String key, boolean enabled, Instant dateCreated) {
        AdminEntity admin = new AdminEntity(
                "Admin" + key,
                "User" + key,
                key + "@example.com",
                Set.of(UserRoles.ADMIN),
                key,
                "encoded-password");
        admin.setEnabled(enabled);
        admin.setDateCreated(Date.from(dateCreated));
        admin.setLastLogin(Date.from(dateCreated.plus(1, ChronoUnit.HOURS)));

        return admin;
    }

    public static ClientEntity client(String key, boolean enabled, Instant dateCreated, long apiKey) {
        ClientEntity client = new ClientEntity(
                "Client" + key,
                "User" + key,
                key + "@example.com",
                Set.of(UserRoles.CLIENT),
                key,
                "encoded-password");
        client.setEnabled(enabled);
        client.setDateCreated(Date.from(dateCreated));
        client.setLastLogin(Date.from(dateCreated.plus(1, ChronoUnit.HOURS)));
        client.setApikey(apiKey);

        return client;
    }

    public static PageableRequest request(
            int page,
            int size,
            List<SortRequest> sort,
            List<FilterRequest> filters) {

        return PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .filters(filters)
                .build();
    }

    public static SortRequest sort(String field, SortDirection direction) {
        return SortRequest.builder()
                .field(field)
                .direction(direction)
                .build();
    }

    public static FilterRequest filter(String field, FilterOperator operator, Object value) {
        return FilterRequest.builder()
                .field(field)
                .operations(List.of(FilterOperationRequest.builder()
                        .operator(operator)
                        .value(value)
                        .build()))
                .build();
    }
}
