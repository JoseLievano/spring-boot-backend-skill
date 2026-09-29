package com.agentForgeBackend.shared.query;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableFactoryTest {

    private final PageableFactory<Object> factory = new PageableFactory<>();

    @Test
    void emptySortUsesProfileDefaultSort() throws Exception {
        PageRequest pageRequest = factory.create(new PageableRequest(), validProfile());

        Sort.Order order = pageRequest.getSort().getOrderFor("id");

        assertEquals(0, pageRequest.getPageNumber());
        assertEquals(20, pageRequest.getPageSize());
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void explicitSortUsesProfileControlledSortProperty() throws Exception {
        PageableRequest request = PageableRequest.builder()
                .sort(List.of(SortRequest.builder()
                        .field("displayName")
                        .direction(SortDirection.DESC)
                        .build()))
                .build();

        PageRequest pageRequest = factory.create(request, validProfile());

        Sort.Order order = pageRequest.getSort().getOrderFor("username");

        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void unknownSortFieldIsRejected() {
        PageableRequest request = requestSortingBy("unknown");

        assertThrows(InvalidQueryRequestException.class, () -> factory.create(request, validProfile()));
    }

    @Test
    void knownButNonSortableFieldIsRejected() {
        PageableRequest request = requestSortingBy("password");

        assertThrows(InvalidQueryRequestException.class, () -> factory.create(request, validProfile()));
    }

    @Test
    void invalidPageMetadataIsRejectedBeforeSpringDataFactoryThrows() {
        PageableRequest request = PageableRequest.builder().page(-1).build();

        assertThrows(InvalidQueryRequestException.class, () -> factory.create(request, validProfile()));
    }

    private static PageableRequest requestSortingBy(String field) {
        return PageableRequest.builder()
                .sort(List.of(SortRequest.builder()
                        .field(field)
                        .direction(SortDirection.ASC)
                        .build()))
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
                                Long.class).sortable("id"),
                        "displayName", QueryableField.string(
                                "displayName",
                                Expressions.stringPath("username")).sortable("username"),
                        "password", QueryableField.string(
                                "password",
                                Expressions.stringPath("password")));
            }

            @Override
            public List<SortRequest> defaultSort() {
                return List.of(SortRequest.builder()
                        .field("id")
                        .direction(SortDirection.ASC)
                        .build());
            }
        };
    }
}
