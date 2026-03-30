package com.authServer.models.hq.client;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ClientForm {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
}
