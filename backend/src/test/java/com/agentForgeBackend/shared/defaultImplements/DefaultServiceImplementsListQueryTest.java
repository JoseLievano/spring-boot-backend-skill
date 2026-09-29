package com.agentForgeBackend.shared.defaultImplements;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.agentForgeBackend.shared.defaultInterfaces.DefaultMapper;
import com.agentForgeBackend.shared.defaultInterfaces.DefaultRepository;
import com.agentForgeBackend.shared.query.EntityQueryProfile;
import com.agentForgeBackend.shared.query.PageableFactory;
import com.agentForgeBackend.shared.query.PageableRequest;
import com.agentForgeBackend.shared.query.QueryPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultServiceImplementsListQueryTest {

    @Mock
    private DefaultRepository<TestEntity, Long> repository;

    @Mock
    private DefaultMapper<TestDTO, TestMiniDTO, TestListDTO, TestForm, TestEntity> mapper;

    @Mock
    private EntityQueryProfile<TestEntity> queryProfile;

    @Mock
    private PageableFactory<TestEntity> pageableFactory;

    @Mock
    private QueryPredicateBuilder<TestEntity> queryPredicateBuilder;

    @Test
    void getListPageBuildsPredicateAndPageableThenMapsThePage() throws Exception {
        PageableRequest request = PageableRequest.builder().page(1).size(10).build();
        Predicate predicate = new BooleanBuilder();
        PageRequest pageRequest = PageRequest.of(1, 10);
        TestEntity entity = new TestEntity(7L, "ada");
        TestListDTO listDTO = new TestListDTO(7L, "ada");

        when(queryPredicateBuilder.build(request, queryProfile)).thenReturn(predicate);
        when(pageableFactory.create(request, queryProfile)).thenReturn(pageRequest);
        when(repository.findAll(predicate, pageRequest)).thenReturn(new PageImpl<>(List.of(entity), pageRequest, 23));
        when(mapper.toListDTO(entity)).thenReturn(listDTO);

        TestService service = new TestService(repository, mapper, queryProfile, pageableFactory, queryPredicateBuilder);

        Page<TestListDTO> result = service.getListPage(request);

        assertEquals(List.of(listDTO), result.getContent());
        assertEquals(23, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals(10, result.getSize());

        InOrder inOrder = inOrder(queryPredicateBuilder, pageableFactory, repository);
        inOrder.verify(queryPredicateBuilder).build(request, queryProfile);
        inOrder.verify(pageableFactory).create(request, queryProfile);
        inOrder.verify(repository).findAll(predicate, pageRequest);
        verify(mapper).toListDTO(entity);
    }

    @Test
    void getListPagePropagatesInvalidQueryRequestsBeforeRepositoryAccess() throws Exception {
        PageableRequest request = new PageableRequest();
        InvalidQueryRequestException expected = new InvalidQueryRequestException("Unknown query field 'x'.");

        when(queryPredicateBuilder.build(request, queryProfile)).thenThrow(expected);

        TestService service = new TestService(repository, mapper, queryProfile, pageableFactory, queryPredicateBuilder);

        InvalidQueryRequestException thrown = assertThrows(
                InvalidQueryRequestException.class,
                () -> service.getListPage(request));

        assertSame(expected, thrown);
        verify(pageableFactory, never()).create(request, queryProfile);
        verify(repository, never()).findAll((Predicate) org.mockito.Mockito.any(), org.mockito.Mockito.any(PageRequest.class));
    }

    private static final class TestService extends DefaultServiceImplements<
            TestDTO,
            TestMiniDTO,
            TestListDTO,
            TestForm,
            TestEntity,
            Long> {

        private TestService(
                DefaultRepository<TestEntity, Long> repository,
                DefaultMapper<TestDTO, TestMiniDTO, TestListDTO, TestForm, TestEntity> mapper,
                EntityQueryProfile<TestEntity> queryProfile,
                PageableFactory<TestEntity> pageableFactory,
                QueryPredicateBuilder<TestEntity> queryPredicateBuilder) {

            super(repository, mapper, queryProfile, pageableFactory, queryPredicateBuilder);
        }
    }

    private record TestEntity(Long id, String name) {
    }

    private record TestDTO(Long id) {
    }

    private record TestMiniDTO(Long id) {
    }

    private record TestListDTO(Long id, String name) {
    }

    private record TestForm(String name) {
    }
}
