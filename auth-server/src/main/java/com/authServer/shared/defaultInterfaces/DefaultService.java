package com.authServer.shared.defaultInterfaces;

import com.authServer.exceptions.InvalidDeleteOperation;
import com.authServer.exceptions.InvalidInsertDetails;
import com.authServer.exceptions.ItemAlreadyExist;
import com.authServer.exceptions.ItemNotFoundException;

import java.util.Collection;

public interface DefaultService <DTO, MINIDTO, FORM, ID>{

    DTO getOne(ID id) throws ItemNotFoundException;

    Collection<DTO> getAll();

    MINIDTO insert(FORM form) throws ItemNotFoundException, ItemAlreadyExist, InvalidInsertDetails;

    DTO update(ID id, FORM form) throws ItemNotFoundException, InvalidInsertDetails;

    DTO delete(ID id) throws ItemNotFoundException, InvalidDeleteOperation;

}