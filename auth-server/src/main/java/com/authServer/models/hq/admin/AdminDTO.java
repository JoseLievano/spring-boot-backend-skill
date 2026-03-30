package com.authServer.models.hq.admin;

import com.authServer.shared.models.baseUser.UserRoles;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class AdminDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Set<String> roles;
}
