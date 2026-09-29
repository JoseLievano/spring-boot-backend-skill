package com.agentForgeBackend.shared.query;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PageableFactory<ENTITY> {

    public PageRequest create(PageableRequest request, EntityQueryProfile<ENTITY> profile)
            throws InvalidQueryRequestException {

        validateRequest(request);
        if (profile == null) {
            throw new IllegalStateException("Entity query profile must not be null.");
        }

        List<SortRequest> requestedSort = request.getSort();
        List<SortRequest> sortRequests = requestedSort.isEmpty() ? profile.defaultSort() : requestedSort;
        Sort sort = buildSort(sortRequests, profile, requestedSort.isEmpty());

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Sort buildSort(
            List<SortRequest> sortRequests,
            EntityQueryProfile<ENTITY> profile,
            boolean defaultSort) throws InvalidQueryRequestException {

        if (sortRequests == null) {
            throw new IllegalStateException("Query profile default sort must not be null.");
        }

        List<Sort.Order> orders = new ArrayList<>(sortRequests.size());
        for (SortRequest sortRequest : sortRequests) {
            orders.add(resolveSortOrder(sortRequest, profile, defaultSort));
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private Sort.Order resolveSortOrder(
            SortRequest sortRequest,
            EntityQueryProfile<ENTITY> profile,
            boolean defaultSort) throws InvalidQueryRequestException {

        try {
            if (sortRequest == null) {
                throw new InvalidQueryRequestException("Sort request must not be null.");
            }
            if (sortRequest.getDirection() == null) {
                throw new InvalidQueryRequestException("Sort direction is required for field '" + sortRequest.getField() + "'.");
            }

            QueryableField<ENTITY, ?> field = profile.requireField(sortRequest.getField());
            if (!field.isSortable()) {
                throw new InvalidQueryRequestException("Field '" + sortRequest.getField() + "' cannot be used for sorting.");
            }

            return new Sort.Order(toSpringDirection(sortRequest.getDirection()), field.sortProperty());
        } catch (InvalidQueryRequestException ex) {
            if (defaultSort) {
                throw new IllegalStateException("Invalid query profile default sort: " + ex.getMessage(), ex);
            }
            throw ex;
        }
    }

    private void validateRequest(PageableRequest request) throws InvalidQueryRequestException {
        if (request == null) {
            throw new InvalidQueryRequestException("Pageable request must not be null.");
        }
        if (request.getPage() < 0) {
            throw new InvalidQueryRequestException("Page index must not be negative.");
        }
        if (request.getSize() < 1 || request.getSize() > 100) {
            throw new InvalidQueryRequestException("Page size must be between 1 and 100.");
        }
        if (request.getSort() == null) {
            throw new InvalidQueryRequestException("Sort list must not be null.");
        }
    }

    private Sort.Direction toSpringDirection(SortDirection direction) {
        return direction == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
