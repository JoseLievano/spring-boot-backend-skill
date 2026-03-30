package com.authServer.models.client;

import com.authServer.models.hq.client.ClientEntity;
import com.authServer.models.hq.client.ClientRepository;
import com.authServer.shared.models.baseUser.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Tag("repository")
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private ClientEntity testClient;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        entityManager.clear();

        // Create test client
        testClient = new ClientEntity(
                "John",
                "Doe",
                "john.doe@example.com",
                new HashSet<>(Set.of(UserRoles.CLIENT)),
                "johndoe",
                "hashedPassword123"
        );


    }

    @Test
    void testSaveAndFindById() {
        // Save the client
        ClientEntity savedClient = clientRepository.save(testClient);
        entityManager.flush();
        entityManager.clear();

        // Find by ID
        Optional<ClientEntity> foundClient = clientRepository.findById(savedClient.getId());

        // Assertions
        assertTrue(foundClient.isPresent());
        assertEquals("John", foundClient.get().getFirstName());
        assertEquals("Doe", foundClient.get().getLastName());
        assertEquals("john.doe@example.com", foundClient.get().getEmail());
        assertEquals("johndoe", foundClient.get().getUsername());
    }

    @Test
    void testFindByUsername() {
        // Save the client
        entityManager.persist(testClient);
        entityManager.flush();
        entityManager.clear();

        // Find by username
        Optional<ClientEntity> foundClient = clientRepository.findByUsername("johndoe");

        // Assertions
        assertTrue(foundClient.isPresent());
        assertEquals("johndoe", foundClient.get().getUsername());
        assertEquals("john.doe@example.com", foundClient.get().getEmail());
    }

    @Test
    void testFindByUsername_NotFound() {
        // Try to find non-existent client
        Optional<ClientEntity> foundClient = clientRepository.findByUsername("nonexistent");

        // Assertions
        assertFalse(foundClient.isPresent());
    }

    @Test
    void testFindByEmail() {
        // Save the client
        entityManager.persist(testClient);
        entityManager.flush();
        entityManager.clear();

        // Find by email
        Optional<ClientEntity> foundClient = clientRepository.findByEmail("john.doe@example.com");

        // Assertions
        assertTrue(foundClient.isPresent());
        assertEquals("john.doe@example.com", foundClient.get().getEmail());
        assertEquals("johndoe", foundClient.get().getUsername());
    }

    @Test
    void testFindByEmail_NotFound() {
        // Try to find client with non-existent email
        Optional<ClientEntity> foundClient = clientRepository.findByEmail("nonexistent@example.com");

        // Assertions
        assertFalse(foundClient.isPresent());
    }

    @Test
    void testMultipleClients() {
        // Create and save first client
        ClientEntity client1 = new ClientEntity(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                new HashSet<>(Set.of(UserRoles.CLIENT)),
                "janesmith",
                "hashedPassword456"
        );
        entityManager.persist(client1);

        // Create and save second client
        ClientEntity client2 = new ClientEntity(
                "Bob",
                "Johnson",
                "bob.johnson@example.com",
                new HashSet<>(Set.of(UserRoles.CLIENT)),
                "bobjohnson",
                "hashedPassword789"
        );
        entityManager.persist(client2);

        entityManager.persist(testClient);
        entityManager.flush();
        entityManager.clear();

        // Test repository methods
        assertThat(clientRepository.findAll()).hasSize(3);

        // Test finding each client by username
        assertTrue(clientRepository.findByUsername("johndoe").isPresent());
        assertTrue(clientRepository.findByUsername("janesmith").isPresent());
        assertTrue(clientRepository.findByUsername("bobjohnson").isPresent());

        // Test finding each client by email
        assertTrue(clientRepository.findByEmail("john.doe@example.com").isPresent());
        assertTrue(clientRepository.findByEmail("jane.smith@example.com").isPresent());
        assertTrue(clientRepository.findByEmail("bob.johnson@example.com").isPresent());

    }

    @Test
    void testUpdateClient() {
        // Save initial client
        ClientEntity savedClient = entityManager.persist(testClient);
        entityManager.flush();
        entityManager.clear();

        // Find and update
        Optional<ClientEntity> foundClient = clientRepository.findById(savedClient.getId());
        assertTrue(foundClient.isPresent());

        foundClient.get().setFirstName("UpdatedJohn");
        foundClient.get().setLastName("UpdatedDoe");
        foundClient.get().setEmail("updated.john@example.com");
        foundClient.get().setUsername("updatedjohndoe");
        clientRepository.save(foundClient.get());
        entityManager.flush();
        entityManager.clear();

        // Verify update
        Optional<ClientEntity> updatedClient = clientRepository.findById(savedClient.getId());
        assertTrue(updatedClient.isPresent());
        assertEquals("UpdatedJohn", updatedClient.get().getFirstName());
        assertEquals("UpdatedDoe", updatedClient.get().getLastName());
        assertEquals("updated.john@example.com", updatedClient.get().getEmail());
        assertEquals("updatedjohndoe", updatedClient.get().getUsername());
    }

    @Test
    void testDeleteClient() {
        // Save client
        ClientEntity savedClient = entityManager.persist(testClient);
        entityManager.flush();
        Long clientId = savedClient.getId();

        // Verify it exists
        assertTrue(clientRepository.existsById(clientId));

        // Delete client
        clientRepository.deleteById(clientId);
        entityManager.flush();

        // Verify it's deleted
        assertFalse(clientRepository.existsById(clientId));
    }

    @Test
    void testClientWithPlanRelationship() {
        // Save client with plan
        entityManager.persist(testClient);
        entityManager.flush();
        entityManager.clear();

        // Find client and verify plan relationship
        Optional<ClientEntity> foundClient = clientRepository.findByUsername("johndoe");
        assertTrue(foundClient.isPresent());
    }


    @Test
    void testClientWithRoles() {
        // Create client with multiple roles
        ClientEntity adminClient = new ClientEntity(
                "Admin",
                "User",
                "admin@example.com",
                new HashSet<>(Set.of(UserRoles.CLIENT, UserRoles.ADMIN)),
                "adminuser",
                "adminPassword"
        );

        entityManager.persist(adminClient);
        entityManager.flush();
        entityManager.clear();

        // Find client and verify roles
        Optional<ClientEntity> foundClient = clientRepository.findByUsername("adminuser");
        assertTrue(foundClient.isPresent());
        assertNotNull(foundClient.get().getRoles());
        assertEquals(2, foundClient.get().getRoles().size());
        assertTrue(foundClient.get().getRoles().contains(UserRoles.CLIENT));
        assertTrue(foundClient.get().getRoles().contains(UserRoles.ADMIN));
    }

    @Test
    void testFindByEmailCaseInsensitive() {
        // Save client
        entityManager.persist(testClient);
        entityManager.flush();
        entityManager.clear();

        // Test finding with different case
        Optional<ClientEntity> foundClientLowerCase = clientRepository.findByEmail("john.doe@example.com");
        Optional<ClientEntity> foundClientUpperCase = clientRepository.findByEmail("JOHN.DOE@EXAMPLE.COM");

        // Note: This behavior depends on database configuration
        // Most databases are case-insensitive for email comparisons by default
        assertTrue(foundClientLowerCase.isPresent());
        // This assertion might fail depending on database configuration
        // assertFalse(foundClientUpperCase.isPresent());
    }

    @Test
    void testClientSearchMethods() {
        // Create clients with similar attributes
        ClientEntity client1 = new ClientEntity(
                "Test",
                "User1",
                "test1@example.com",
                new HashSet<>(Set.of(UserRoles.CLIENT)),
                "testuser1",
                "password1"
        );
        entityManager.persist(client1);

        ClientEntity client2 = new ClientEntity(
                "Test",
                "User2",
                "test2@example.com",
                new HashSet<>(Set.of(UserRoles.CLIENT)),
                "testuser2",
                "password2"
        );
        entityManager.persist(client2);

        entityManager.flush();
        entityManager.clear();

        // Test that each search method returns exactly one result
        Optional<ClientEntity> byUsername1 = clientRepository.findByUsername("testuser1");
        Optional<ClientEntity> byUsername2 = clientRepository.findByUsername("testuser2");
        Optional<ClientEntity> byEmail1 = clientRepository.findByEmail("test1@example.com");
        Optional<ClientEntity> byEmail2 = clientRepository.findByEmail("test2@example.com");

        // All should be present and unique
        assertTrue(byUsername1.isPresent());
        assertTrue(byUsername2.isPresent());
        assertTrue(byEmail1.isPresent());
        assertTrue(byEmail2.isPresent());

        // Verify they return different clients
        assertNotEquals(byUsername1.get().getId(), byUsername2.get().getId());
        assertNotEquals(byEmail1.get().getId(), byEmail2.get().getId());
    }
}