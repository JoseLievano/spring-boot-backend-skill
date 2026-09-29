package com.agentForgeBackend.models.hq.client;

import com.agentForgeBackend.shared.query.EntityQueryProfile;
import com.agentForgeBackend.shared.query.QueryableField;
import com.agentForgeBackend.shared.query.SortDirection;
import com.agentForgeBackend.shared.query.SortRequest;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class ClientQueryProfile implements EntityQueryProfile<ClientEntity> {

    private static final QClientEntity CLIENT = QClientEntity.clientEntity;

    private static final Map<String, QueryableField<ClientEntity, ?>> FIELDS = Map.of(
            "id", QueryableField.<ClientEntity, Long>number("id", CLIENT.id, Long.class).sortable("id"),
            "firstName", QueryableField.<ClientEntity>string("firstName", CLIENT.firstName).sortable("firstName"),
            "lastName", QueryableField.<ClientEntity>string("lastName", CLIENT.lastName).sortable("lastName"),
            "email", QueryableField.<ClientEntity>string("email", CLIENT.email).sortable("email"),
            "username", QueryableField.<ClientEntity>string("username", CLIENT.username).sortable("username"),
            "enabled", QueryableField.<ClientEntity>booleanField("enabled", CLIENT.enabled).sortable("enabled"),
            "dateCreated", QueryableField.<ClientEntity, Date>dateTime("dateCreated", CLIENT.dateCreated, Date.class)
                    .sortable("dateCreated"),
            "lastLogin", QueryableField.<ClientEntity, Date>dateTime("lastLogin", CLIENT.lastLogin, Date.class)
                    .sortable("lastLogin")
    );

    private static final List<SortRequest> DEFAULT_SORT = List.of(SortRequest.builder()
            .field("id")
            .direction(SortDirection.ASC)
            .build());

    @Override
    public Map<String, QueryableField<ClientEntity, ?>> fields() {
        return FIELDS;
    }

    @Override
    public List<SortRequest> defaultSort() {
        return DEFAULT_SORT;
    }
}
