package com.authServer.shared.models.baseUser;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Table (name = "base_user")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column
    @Size(min = 2, max = 100)
    private String firstName;

    @Column(length = 100)
    @Size(min = 2, max = 100)
    private String lastName;

    @Column(nullable = false, unique = true)
    @Size(min = 4, max = 100)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    private Set<UserRoles> roles = new HashSet<>();

    @Column(nullable = false, unique = true)
    @Size(min = 2, max = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private Date dateCreated;

    @Column
    private Date lastLogin;

    @Column(nullable = false)
    private boolean accountNonExpired = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(nullable = false)
    private boolean enabled = true;

    public BaseUserEntity(
            String firstName,
            String lastName,
            String email,
            Set<UserRoles> roles,
            String username,
            String password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.username = username;
        this.password = password;
    }
}