package com.authServer.models.hq.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class AdminForm {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    @NotBlank(message = "username is require")
    private String username;

    private Set<String> roles;

}
