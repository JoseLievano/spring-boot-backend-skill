package com.agentForgeBackend.shared.query;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.agentForgeBackend.shared.models.baseUser.UserRoles;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryableFieldTest {

    @Test
    void stringFieldRequiresStringValuesAndSupportsTextOperators() throws Exception {
        QueryableField<Object, String> field = QueryableField.string(
                "username",
                Expressions.stringPath("username"));

        assertEquals("alice", field.convertValue("alice"));
        assertDoesNotThrow(() -> field.buildPredicate(FilterOperator.CONTAINS, "ali"));
        assertDoesNotThrow(() -> field.buildPredicate(FilterOperator.STARTS_WITH, "a"));
        assertDoesNotThrow(() -> field.buildPredicate(FilterOperator.ENDS_WITH, "e"));

        assertThrows(InvalidQueryRequestException.class, () -> field.convertValue(12));
        assertThrows(InvalidQueryRequestException.class, () -> field.buildPredicate(FilterOperator.GREATER_THAN, "alice"));
    }

    @Test
    void numberFieldRejectsNonNumericAndFractionalIntegralValues() throws Exception {
        QueryableField<Object, Long> field = QueryableField.number(
                "id",
                Expressions.numberPath(Long.class, "id"),
                Long.class);

        assertEquals(12L, field.convertValue(12));
        assertEquals(List.of(1L, 2L), field.convertValues(List.of(1, 2L)));
        assertDoesNotThrow(() -> field.buildPredicate(FilterOperator.GREATER_THAN_OR_EQUAL, 10));

        assertThrows(InvalidQueryRequestException.class, () -> field.convertValue("12"));
        assertThrows(InvalidQueryRequestException.class, () -> field.convertValue(12.5));
        assertThrows(InvalidQueryRequestException.class, () -> field.buildPredicate(FilterOperator.IN, 12));
    }

    @Test
    void booleanFieldRequiresBooleanValuesAndRejectsStringGuessing() throws Exception {
        QueryableField<Object, Boolean> field = QueryableField.booleanField(
                "enabled",
                Expressions.booleanPath("enabled"));

        assertEquals(Boolean.TRUE, field.convertValue(true));
        assertTrue(field.supports(FilterOperator.EQUALS));
        assertFalse(field.supports(FilterOperator.IN));

        assertThrows(InvalidQueryRequestException.class, () -> field.convertValue("true"));
    }

    @Test
    void enumFieldRequiresExactEnumNames() throws Exception {
        QueryableField<Object, UserRoles> field = QueryableField.enumField(
                "role",
                Expressions.enumPath(UserRoles.class, "role"),
                UserRoles.class);

        assertEquals(UserRoles.ADMIN, field.convertValue("ADMIN"));
        assertDoesNotThrow(() -> field.buildPredicate(FilterOperator.IN, List.of("ADMIN", "CLIENT")));

        assertThrows(InvalidQueryRequestException.class, () -> field.convertValue("admin"));
        assertThrows(InvalidQueryRequestException.class, () -> field.convertValue(1));
    }

    @Test
    void dateTimeFieldParsesIsoInstantStrings() throws Exception {
        QueryableField<Object, Date> field = QueryableField.dateTime(
                "dateCreated",
                Expressions.dateTimePath(Date.class, "dateCreated"),
                Date.class);

        Date expected = Date.from(Instant.parse("2026-04-27T10:15:30Z"));

        assertEquals(expected, field.convertValue("2026-04-27T10:15:30Z"));
        assertDoesNotThrow(() -> field.buildPredicate(FilterOperator.LESS_THAN, "2026-04-28T00:00:00Z"));

        assertThrows(InvalidQueryRequestException.class, () -> field.convertValue("not-a-date"));
    }

    @Test
    void dateFieldParsesIsoLocalDateStrings() throws Exception {
        QueryableField<Object, LocalDate> field = QueryableField.date(
                "birthday",
                Expressions.datePath(LocalDate.class, "birthday"),
                LocalDate.class);

        assertEquals(LocalDate.of(2026, 4, 27), field.convertValue("2026-04-27"));
        assertDoesNotThrow(() -> field.buildPredicate(FilterOperator.GREATER_THAN_OR_EQUAL, "2026-04-01"));
    }

    @Test
    void nullOperatorsAreOptInPerField() {
        QueryableField<Object, String> field = QueryableField.string(
                "email",
                Expressions.stringPath("email"));

        assertFalse(field.supports(FilterOperator.IS_NULL));
        assertThrows(InvalidQueryRequestException.class, () -> field.buildPredicate(FilterOperator.IS_NULL, null));

        QueryableField<Object, String> nullableField = field.nullable();

        assertTrue(nullableField.supports(FilterOperator.IS_NULL));
        assertDoesNotThrow(() -> nullableField.buildPredicate(FilterOperator.IS_NULL, "ignored"));
        assertDoesNotThrow(() -> nullableField.buildPredicate(FilterOperator.IS_NOT_NULL, "ignored"));
    }
}
