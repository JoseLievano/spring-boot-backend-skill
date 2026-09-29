package com.agentForgeBackend.shared.defaultInterfaces;

import com.agentForgeBackend.exceptions.InvalidDeleteOperation;
import com.agentForgeBackend.exceptions.InvalidInsertDetails;
import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.agentForgeBackend.exceptions.ItemAlreadyExist;
import com.agentForgeBackend.exceptions.ItemNotFoundException;
import com.agentForgeBackend.shared.query.PageableRequest;
import org.springframework.data.domain.Page;

import java.util.Collection;

public interface DefaultService <DTO, MINIDTO, LISTDTO, FORM, ID>{

    DTO getOne(ID id) throws ItemNotFoundException;

    Collection<DTO> getAll();

    MINIDTO insert(FORM form) throws ItemNotFoundException, ItemAlreadyExist, InvalidInsertDetails;

    DTO update(ID id, FORM form) throws ItemNotFoundException, InvalidInsertDetails;

    DTO delete(ID id) throws ItemNotFoundException, InvalidDeleteOperation;

    Page<LISTDTO> getListPage(PageableRequest request) throws InvalidQueryRequestException;

}
