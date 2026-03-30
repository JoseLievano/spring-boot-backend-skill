package com.authServer.models.hq.client;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ClientDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Set<String> roles;
    private String apikey;
}
