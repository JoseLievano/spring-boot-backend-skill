package com.agentForgeBackend.models.hq.client;

import lombok.*;

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
