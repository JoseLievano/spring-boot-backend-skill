package com.authServer.testUtils;

import com.authServer.configuration.filter.JwtTokenService;
import com.authServer.models.hq.admin.AdminEntity;
import com.authServer.models.hq.client.ClientEntity;
import com.authServer.shared.models.baseUser.UserRoles;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Transactional
public class TestAuthenticationHelper {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    private AdminEntity adminUser;
    private ClientEntity clientUser;
    private String adminToken;
    private String clientToken;

    /**
     * Initialize mock users with different roles and generate JWT tokens for them
     * Call this method in your @BeforeEach or at the beginning of your tests
     */
    public void initializeMockUsers() {
        // Clear the entity manager cache first
        entityManager.clear();

        // Create and persist ADMIN user
        adminUser = new AdminEntity();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setUsername("admin@test.com");
        adminUser.setPassword("encodedPassword123");
        adminUser.setRoles(Set.of(UserRoles.ADMIN));
        adminUser.setEnabled(true);
        adminUser.setAccountNonExpired(true);
        adminUser.setAccountNonLocked(true);
        adminUser.setCredentialsNonExpired(true);

        entityManager.persist(adminUser);
        entityManager.flush();

        // Create and persist CLIENT user
        clientUser = new ClientEntity();
        clientUser.setFirstName("Client");
        clientUser.setLastName("User");
        clientUser.setEmail("client@test.com");
        clientUser.setUsername("client@test.com");
        clientUser.setPassword("encodedPassword123");
        clientUser.setRoles(Set.of(UserRoles.CLIENT));
        clientUser.setEnabled(true);
        clientUser.setAccountNonExpired(true);
        clientUser.setAccountNonLocked(true);
        clientUser.setCredentialsNonExpired(true);

        entityManager.persist(clientUser);
        entityManager.flush();

        // Generate JWT tokens for each user
        adminToken = "Bearer " + jwtTokenService.generateToken(
                adminUser,
                List.of(new SimpleGrantedAuthority(UserRoles.ADMIN.getAuthority()))
        );

        clientToken = "Bearer " + jwtTokenService.generateToken(
                clientUser,
                List.of(new SimpleGrantedAuthority(UserRoles.CLIENT.getAuthority()))
        );

        // Clear entity manager to ensure clean state for tests
        entityManager.clear();
    }

    // Getters for tokens and users
    public String getAdminToken() {
        if (adminToken == null) {
            throw new IllegalStateException("Admin token not initialized. Call initializeMockUsers() first.");
        }
        return adminToken;
    }

    public String getClientToken() {
        if (clientToken == null) {
            throw new IllegalStateException("Client token not initialized. Call initializeMockUsers() first.");
        }
        return clientToken;
    }

    public AdminEntity getAdminUser() {
        return adminUser;
    }

    public ClientEntity getClientUser() {
        return clientUser;
    }

    /**
     * Clean up method if needed
     */
    public void cleanUp() {
        entityManager.clear();
        adminToken = null;
        clientToken = null;
        adminUser = null;
        clientUser = null;
    }
}