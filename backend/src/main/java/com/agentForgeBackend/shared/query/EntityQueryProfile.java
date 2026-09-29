package com.agentForgeBackend.shared.query;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;

import java.util.List;
import java.util.Map;

public interface EntityQueryProfile<ENTITY> {

    Map<String, QueryableField<ENTITY, ?>> fields();

    default List<SortRequest> defaultSort() {
        return List.of();
    }

    default QueryableField<ENTITY, ?> requireField(String field) throws InvalidQueryRequestException {
        if (field == null || field.isBlank()) {
            throw new InvalidQueryRequestException("Query field is required.");
        }

        Map<String, QueryableField<ENTITY, ?>> availableFields = fields();
        if (availableFields == null) {
            throw new IllegalStateException("Query profile fields must not be null.");
        }

        QueryableField<ENTITY, ?> queryableField = availableFields.get(field);
        if (queryableField == null) {
            throw new InvalidQueryRequestException("Unknown query field '" + field + "'.");
        }

        return queryableField;
    }
}
