package com.agentForgeBackend.shared.defaultInterfaces;

public interface DefaultMapper <DTO, MINIDTO, LISTDTO, FORM, ENTITY>{

    DTO toDTO (ENTITY entity);

    MINIDTO toSmallDTO(ENTITY entity);

    LISTDTO toListDTO(ENTITY entity);

    ENTITY toEntity(FORM form);


}
