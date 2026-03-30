package com.authServer.shared.models.baseUser;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class BaseUserDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Set<String> roles;
}
