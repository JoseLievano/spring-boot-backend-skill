package com.agentForgeBackend.shared.defaultImplements;

import com.agentForgeBackend.exceptions.InvalidDeleteOperation;
import com.agentForgeBackend.exceptions.InvalidInsertDetails;
import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.agentForgeBackend.exceptions.ItemAlreadyExist;
import com.agentForgeBackend.exceptions.ItemNotFoundException;
import com.agentForgeBackend.shared.defaultInterfaces.DefaultService;
import com.agentForgeBackend.shared.query.PageableRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

public abstract class DefaultController <DTO, MINIDTO, LISTDTO, FORM, ID>{

    protected final DefaultService<DTO, MINIDTO, LISTDTO, FORM, ID> defaultService;

    protected DefaultController(DefaultService <DTO, MINIDTO, LISTDTO, FORM, ID> defaultService){
        this.defaultService = defaultService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTO> getOne(@PathVariable ID id) throws ItemNotFoundException{
        return ResponseEntity.ok(defaultService.getOne(id));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Collection<DTO>> getAll(){
        return ResponseEntity.ok(defaultService.getAll());
    }

    @PostMapping("/list")
    public ResponseEntity<Page<LISTDTO>> getListPage(@Valid @RequestBody PageableRequest request)
            throws InvalidQueryRequestException {
        return ResponseEntity.ok(defaultService.getListPage(request));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<MINIDTO> insert (@Valid @RequestBody FORM form) throws ItemNotFoundException,
            ItemAlreadyExist, InvalidInsertDetails {
        return ResponseEntity.ok(defaultService.insert(form));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DTO> update (@PathVariable ID id, @Valid @RequestBody FORM form) throws ItemNotFoundException,
            InvalidInsertDetails{
        return ResponseEntity.ok(defaultService.update(id, form));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DTO> delete (@PathVariable ID id) throws ItemNotFoundException,
            InvalidDeleteOperation{
        return ResponseEntity.ok(defaultService.delete(id));
    }
    
}
