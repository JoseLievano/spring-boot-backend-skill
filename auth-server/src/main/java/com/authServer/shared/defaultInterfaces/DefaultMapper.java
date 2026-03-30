package com.authServer.shared.defaultInterfaces;

public interface DefaultMapper <DTO, MINIDTO, FORM, ENTITY>{

    DTO toDTO (ENTITY entity);

    MINIDTO toSmallDTO(ENTITY entity);

    ENTITY toEntity(FORM form);


}