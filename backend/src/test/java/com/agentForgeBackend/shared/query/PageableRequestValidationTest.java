package com.agentForgeBackend.shared.query;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageableRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void emptyRequestUsesDefaultsAndPasses() {
        PageableRequest request = new PageableRequest();

        Set<ConstraintViolation<PageableRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), () -> violations.toString());
        assertEquals(0, request.getPage());
        assertEquals(20, request.getSize());
        assertTrue(request.getSort().isEmpty());
        assertTrue(request.getFilters().isEmpty());
    }

    @Test
    void negativePageFails() {
        PageableRequest request = PageableRequest.builder().page(-1).build();

        assertSingleViolationOn(request, "page");
    }

    @Test
    void sizeBelowOneFails() {
        PageableRequest request = PageableRequest.builder().size(0).build();

        assertSingleViolationOn(request, "size");
    }

    @Test
    void sizeAboveOneHundredFails() {
        PageableRequest request = PageableRequest.builder().size(101).build();

        assertSingleViolationOn(request, "size");
    }

    @Test
    void nullSortListFails() {
        PageableRequest request = PageableRequest.builder().sort(null).build();

        assertSingleViolationOn(request, "sort");
    }

    @Test
    void nullFiltersListFails() {
        PageableRequest request = PageableRequest.builder().filters(null).build();

        assertSingleViolationOn(request, "filters");
    }

    @Test
    void blankSortFieldFailsViaCascade() {
        SortRequest sort = SortRequest.builder().field(" ").direction(SortDirection.ASC).build();
        PageableRequest request = PageableRequest.builder().sort(List.of(sort)).build();

        assertSingleViolationOn(request, "sort[0].field");
    }

    @Test
    void longSortFieldFailsViaCascade() {
        SortRequest sort = SortRequest.builder().field("a".repeat(65)).direction(SortDirection.ASC).build();
        PageableRequest request = PageableRequest.builder().sort(List.of(sort)).build();

        assertSingleViolationOn(request, "sort[0].field");
    }

    @Test
    void nullSortDirectionFailsViaCascade() {
        SortRequest sort = SortRequest.builder().field("username").direction(null).build();
        PageableRequest request = PageableRequest.builder().sort(List.of(sort)).build();

        assertSingleViolationOn(request, "sort[0].direction");
    }

    @Test
    void blankFilterFieldFailsViaCascade() {
        FilterRequest filter = FilterRequest.builder()
                .field(" ")
                .operations(List.of(validOperation()))
                .build();
        PageableRequest request = PageableRequest.builder().filters(List.of(filter)).build();

        assertSingleViolationOn(request, "filters[0].field");
    }

    @Test
    void longFilterFieldFailsViaCascade() {
        FilterRequest filter = FilterRequest.builder()
                .field("a".repeat(65))
                .operations(List.of(validOperation()))
                .build();
        PageableRequest request = PageableRequest.builder().filters(List.of(filter)).build();

        assertSingleViolationOn(request, "filters[0].field");
    }

    @Test
    void nullOperationsListFailsViaCascade() {
        FilterRequest filter = FilterRequest.builder().field("email").operations(null).build();
        PageableRequest request = PageableRequest.builder().filters(List.of(filter)).build();

        assertSingleViolationOn(request, "filters[0].operations");
    }

    @Test
    void emptyOperationsListFailsViaCascade() {
        FilterRequest filter = FilterRequest.builder().field("email").operations(List.of()).build();
        PageableRequest request = PageableRequest.builder().filters(List.of(filter)).build();

        assertSingleViolationOn(request, "filters[0].operations");
    }

    @Test
    void nullOperatorFailsViaDoubleCascade() {
        FilterOperationRequest operation = FilterOperationRequest.builder().operator(null).value("x").build();
        FilterRequest filter = FilterRequest.builder().field("email").operations(List.of(operation)).build();
        PageableRequest request = PageableRequest.builder().filters(List.of(filter)).build();

        assertSingleViolationOn(request, "filters[0].operations[0].operator");
    }

    private static FilterOperationRequest validOperation() {
        return FilterOperationRequest.builder()
                .operator(FilterOperator.EQUALS)
                .value("x")
                .build();
    }

    private static void assertSingleViolationOn(PageableRequest request, String propertyPathPrefix) {
        Set<ConstraintViolation<PageableRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), () -> violations.toString());
        assertTrue(
                violations.iterator().next().getPropertyPath().toString().startsWith(propertyPathPrefix),
                () -> violations.toString());
    }
}
