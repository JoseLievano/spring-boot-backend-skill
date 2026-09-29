package com.agentForgeBackend.shared.defaultImplements;

import com.agentForgeBackend.exceptions.InvalidDeleteOperation;
import com.agentForgeBackend.exceptions.InvalidInsertDetails;
import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.agentForgeBackend.exceptions.ItemAlreadyExist;
import com.agentForgeBackend.exceptions.ItemNotFoundException;
import com.agentForgeBackend.shared.defaultInterfaces.DefaultMapper;
import com.agentForgeBackend.shared.defaultInterfaces.DefaultRepository;
import com.agentForgeBackend.shared.defaultInterfaces.DefaultService;
import com.agentForgeBackend.shared.query.EntityQueryProfile;
import com.agentForgeBackend.shared.query.PageableFactory;
import com.agentForgeBackend.shared.query.PageableRequest;
import com.agentForgeBackend.shared.query.QueryPredicateBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Transactional(rollbackFor = {
        ItemNotFoundException.class,
        InvalidInsertDetails.class,
        InvalidDeleteOperation.class,
        ItemAlreadyExist.class,
        InvalidQueryRequestException.class
})
public abstract class DefaultServiceImplements <DTO, MINIDTO, LISTDTO, FORM, ENTITY, ID>
        implements DefaultService  <DTO, MINIDTO, LISTDTO, FORM, ID>{

    protected final DefaultRepository<ENTITY, ID> repository;
    protected final DefaultMapper <DTO, MINIDTO, LISTDTO, FORM, ENTITY> mapper;
    protected final EntityQueryProfile<ENTITY> queryProfile;
    private final PageableFactory<ENTITY> pageableFactory;
    private final QueryPredicateBuilder<ENTITY> queryPredicateBuilder;

    public DefaultServiceImplements(DefaultRepository<ENTITY, ID> repository,
                                    DefaultMapper <DTO, MINIDTO, LISTDTO, FORM, ENTITY> mapper,
                                    EntityQueryProfile<ENTITY> queryProfile,
                                    PageableFactory<ENTITY> pageableFactory,
                                    QueryPredicateBuilder<ENTITY> queryPredicateBuilder)
    {
        this.repository = repository;
        this.mapper = mapper;
        this.queryProfile = queryProfile;
        this.pageableFactory = pageableFactory;
        this.queryPredicateBuilder = queryPredicateBuilder;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public DTO getOne(ID id) throws ItemNotFoundException {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(ItemNotFoundException::new);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Collection<DTO> getAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toSet());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Page<LISTDTO> getListPage(PageableRequest request) throws InvalidQueryRequestException {
        Predicate predicate = queryPredicateBuilder.build(request, queryProfile);
        PageRequest pageRequest = pageableFactory.create(request, queryProfile);

        return repository.findAll(predicate, pageRequest)
                .map(mapper::toListDTO);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public MINIDTO insert (FORM form) throws ItemNotFoundException, ItemAlreadyExist,
            InvalidInsertDetails{
        return mapper.toSmallDTO(repository.save(mapper.toEntity(form)));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public DTO update(ID id, FORM form) throws ItemNotFoundException, InvalidInsertDetails{
        ENTITY toUpdate = repository.findById(id).orElseThrow(ItemNotFoundException::new);
        repository.save(toUpdate);
        return mapper.toDTO(toUpdate);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public DTO delete(ID id) throws ItemNotFoundException, InvalidDeleteOperation {
        ENTITY toDelete = repository.findById(id).orElseThrow(ItemNotFoundException::new);
        repository.delete(toDelete);
        return mapper.toDTO(toDelete);
    }
}
