package com.agentForgeBackend.models.hq.admin;

import com.agentForgeBackend.shared.query.EntityQueryProfile;
import com.agentForgeBackend.shared.query.QueryableField;
import com.agentForgeBackend.shared.query.SortDirection;
import com.agentForgeBackend.shared.query.SortRequest;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class AdminQueryProfile implements EntityQueryProfile<AdminEntity> {

    private static final QAdminEntity ADMIN = QAdminEntity.adminEntity;

    private static final Map<String, QueryableField<AdminEntity, ?>> FIELDS = Map.of(
            "id", QueryableField.<AdminEntity, Long>number("id", ADMIN.id, Long.class).sortable("id"),
            "firstName", QueryableField.<AdminEntity>string("firstName", ADMIN.firstName).sortable("firstName"),
            "lastName", QueryableField.<AdminEntity>string("lastName", ADMIN.lastName).sortable("lastName"),
            "email", QueryableField.<AdminEntity>string("email", ADMIN.email).sortable("email"),
            "username", QueryableField.<AdminEntity>string("username", ADMIN.username).sortable("username"),
            "enabled", QueryableField.<AdminEntity>booleanField("enabled", ADMIN.enabled).sortable("enabled"),
            "dateCreated", QueryableField.<AdminEntity, Date>dateTime("dateCreated", ADMIN.dateCreated, Date.class)
                    .sortable("dateCreated"),
            "lastLogin", QueryableField.<AdminEntity, Date>dateTime("lastLogin", ADMIN.lastLogin, Date.class)
                    .sortable("lastLogin")
    );

    private static final List<SortRequest> DEFAULT_SORT = List.of(SortRequest.builder()
            .field("id")
            .direction(SortDirection.ASC)
            .build());

    @Override
    public Map<String, QueryableField<AdminEntity, ?>> fields() {
        return FIELDS;
    }

    @Override
    public List<SortRequest> defaultSort() {
        return DEFAULT_SORT;
    }
}
