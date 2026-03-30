package com.authServer.shared.defaultImplements;

import com.authServer.exceptions.InvalidDeleteOperation;
import com.authServer.exceptions.InvalidInsertDetails;
import com.authServer.exceptions.ItemAlreadyExist;
import com.authServer.exceptions.ItemNotFoundException;
import com.authServer.shared.defaultInterfaces.DefaultMapper;
import com.authServer.shared.defaultInterfaces.DefaultRepository;
import com.authServer.shared.defaultInterfaces.DefaultService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Transactional(rollbackFor = {
        ItemNotFoundException.class,
        InvalidInsertDetails.class,
        InvalidDeleteOperation.class,
        ItemAlreadyExist.class
})
public abstract class DefaultServiceImplements <DTO, MINIDTO, FORM, ENTITY, ID> implements DefaultService  <DTO, MINIDTO, FORM, ID>{

   protected final DefaultRepository<ENTITY, ID> repository;
   protected final DefaultMapper <DTO, MINIDTO, FORM, ENTITY> mapper;

   public DefaultServiceImplements(DefaultRepository<ENTITY, ID> repository,
                                   DefaultMapper <DTO, MINIDTO, FORM, ENTITY> mapper)
   {
       this.repository = repository;
       this.mapper = mapper;
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