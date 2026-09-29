package com.agentForgeBackend.models.hq.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ClientListDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Set<String> roles;
    private boolean enabled;
    private Date dateCreated;
    private Date lastLogin;
}
