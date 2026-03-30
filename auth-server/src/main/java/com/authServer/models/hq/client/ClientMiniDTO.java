package com.authServer.models.hq.client;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ClientMiniDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Set<String> roles;
    private String apiKey;
}
