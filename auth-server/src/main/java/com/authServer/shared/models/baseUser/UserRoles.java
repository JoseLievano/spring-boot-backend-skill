package com.authServer.shared.models.baseUser;

public enum UserRoles {
    ADMIN,
    CLIENT,
    EMPLOYEE;

    public String getAuthority(){
        return "ROLE_" + this.name();
    }
}