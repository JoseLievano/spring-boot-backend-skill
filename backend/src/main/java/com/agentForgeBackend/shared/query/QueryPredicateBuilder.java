package com.agentForgeBackend.shared.query;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueryPredicateBuilder<ENTITY> {

    public Predicate build(PageableRequest request, EntityQueryProfile<ENTITY> profile)
            throws InvalidQueryRequestException {

        if (request == null) {
            throw new InvalidQueryRequestException("Pageable request must not be null.");
        }

        return build(request.getFilters(), profile);
    }

    public Predicate build(List<FilterRequest> filters, EntityQueryProfile<ENTITY> profile)
            throws InvalidQueryRequestException {

        if (filters == null) {
            throw new InvalidQueryRequestException("Filter list must not be null.");
        }
        if (profile == null) {
            throw new IllegalStateException("Entity query profile must not be null.");
        }

        BooleanBuilder predicate = new BooleanBuilder();
        for (FilterRequest filter : filters) {
            predicate.and(buildFilterGroup(filter, profile));
        }

        return predicate;
    }

    private Predicate buildFilterGroup(FilterRequest filter, EntityQueryProfile<ENTITY> profile)
            throws InvalidQueryRequestException {

        if (filter == null) {
            throw new InvalidQueryRequestException("Filter request must not be null.");
        }
        if (filter.getOperations() == null || filter.getOperations().isEmpty()) {
            throw new InvalidQueryRequestException("Filter field '" + filter.getField() + "' requires at least one operation.");
        }

        QueryableField<ENTITY, ?> field = profile.requireField(filter.getField());
        BooleanBuilder group = new BooleanBuilder();

        for (FilterOperationRequest operation : filter.getOperations()) {
            if (operation == null) {
                throw new InvalidQueryRequestException("Filter operation must not be null for field '" + filter.getField() + "'.");
            }
            group.or(field.buildPredicate(operation.getOperator(), operation.getValue()));
        }

        return group;
    }
}
