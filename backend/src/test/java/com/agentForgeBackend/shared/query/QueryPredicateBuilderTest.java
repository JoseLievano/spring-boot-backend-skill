package com.agentForgeBackend.shared.query;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryPredicateBuilderTest {

    private final QueryPredicateBuilder<Object> builder = new QueryPredicateBuilder<>();

    @Test
    void emptyFiltersReturnNonNullNeutralPredicate() throws Exception {
        Predicate predicate = builder.build(List.of(), validProfile());

        assertNotNull(predicate);
    }

    @Test
    void unknownFilterFieldIsRejected() {
        FilterRequest filter = filter("unknown", operation(FilterOperator.EQUALS, "x"));

        assertThrows(InvalidQueryRequestException.class, () -> builder.build(List.of(filter), validProfile()));
    }

    @Test
    void unsupportedOperatorIsRejectedForField() {
        FilterRequest filter = filter("id", operation(FilterOperator.CONTAINS, 1));

        assertThrows(InvalidQueryRequestException.class, () -> builder.build(List.of(filter), validProfile()));
    }

    @Test
    void invalidValueTypeIsRejectedForField() {
        FilterRequest filter = filter("enabled", operation(FilterOperator.EQUALS, "true"));

        assertThrows(InvalidQueryRequestException.class, () -> builder.build(List.of(filter), validProfile()));
    }

    @Test
    void operationsWithinFilterUseOrAndSeparateFiltersUseAnd() throws Exception {
        FilterRequest usernameFilter = filter(
                "username",
                operation(FilterOperator.CONTAINS, "ali"),
                operation(FilterOperator.STARTS_WITH, "bo"));
        FilterRequest enabledFilter = filter("enabled", operation(FilterOperator.EQUALS, true));

        Predicate predicate = builder.build(List.of(usernameFilter, enabledFilter), validProfile());

        String predicateText = predicate.toString();

        assertTrue(predicateText.contains("||"), predicateText);
        assertTrue(predicateText.contains("&&"), predicateText);
        assertTrue(predicateText.contains("username"), predicateText);
        assertTrue(predicateText.contains("enabled"), predicateText);
    }

    private static FilterRequest filter(String field, FilterOperationRequest... operations) {
        return FilterRequest.builder()
                .field(field)
                .operations(List.of(operations))
                .build();
    }

    private static FilterOperationRequest operation(FilterOperator operator, Object value) {
        return FilterOperationRequest.builder()
                .operator(operator)
                .value(value)
                .build();
    }

    private static EntityQueryProfile<Object> validProfile() {
        return new EntityQueryProfile<>() {
            @Override
            public Map<String, QueryableField<Object, ?>> fields() {
                return Map.of(
                        "id", QueryableField.number(
                                "id",
                                Expressions.numberPath(Long.class, "id"),
                                Long.class),
                        "username", QueryableField.string(
                                "username",
                                Expressions.stringPath("username")),
                        "enabled", QueryableField.booleanField(
                                "enabled",
                                Expressions.booleanPath("enabled")));
            }

            @Override
            public List<SortRequest> defaultSort() {
                return List.of();
            }
        };
    }
}
