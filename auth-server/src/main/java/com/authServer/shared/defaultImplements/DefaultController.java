package com.authServer.shared.defaultImplements;

import com.authServer.exceptions.InvalidDeleteOperation;
import com.authServer.exceptions.InvalidInsertDetails;
import com.authServer.exceptions.ItemAlreadyExist;
import com.authServer.exceptions.ItemNotFoundException;
import com.authServer.shared.defaultInterfaces.DefaultService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

public abstract class DefaultController <DTO, MINIDTO, FORM, ID>{

    protected final DefaultService<DTO, MINIDTO, FORM, ID> defaultService;

    protected DefaultController(DefaultService <DTO, MINIDTO, FORM, ID> defaultService){
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