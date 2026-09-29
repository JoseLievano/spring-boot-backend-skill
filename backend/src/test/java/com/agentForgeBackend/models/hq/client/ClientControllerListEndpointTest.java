package com.agentForgeBackend.models.hq.client;

import com.agentForgeBackend.models.hq.ListQueryTestDataFactory;
import com.agentForgeBackend.shared.query.FilterOperator;
import com.agentForgeBackend.shared.query.PageableRequest;
import com.agentForgeBackend.shared.query.SortDirection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "task6-client-controller", roles = "ADMIN")
class ClientControllerListEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    private ClientEntity alpha;
    private ClientEntity gamma;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        clientRepository.flush();

        alpha = clientRepository.save(ListQueryTestDataFactory.client("client-controller-alpha", true, ListQueryTestDataFactory.EARLY_DATE, 30_001L));
        clientRepository.save(ListQueryTestDataFactory.client("client-controller-beta", false, ListQueryTestDataFactory.MIDDLE_DATE, 30_002L));
        gamma = clientRepository.save(ListQueryTestDataFactory.client("client-controller-gamma", true, ListQueryTestDataFactory.LATE_DATE, 30_003L));
        clientRepository.flush();
    }

    @Test
    void listWithDefaultRequestReturnsClientListPageWithoutSensitiveFields() throws Exception {
        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id").value(alpha.getId().intValue()))
                .andExpect(jsonPath("$.content[0].username").value("client-controller-alpha"))
                .andExpect(jsonPath("$.content[0].roles", hasItem("ROLE_CLIENT")))
                .andExpect(jsonPath("$.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.content[0].apikey").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void listWithFilterSortAndPaginationReturnsMatchingClientPage() throws Exception {
        PageableRequest request = ListQueryTestDataFactory.request(
                0,
                1,
                List.of(ListQueryTestDataFactory.sort("username", SortDirection.DESC)),
                List.of(ListQueryTestDataFactory.filter("enabled", FilterOperator.EQUALS, true)));

        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(gamma.getId().intValue()))
                .andExpect(jsonPath("$.content[0].username").value("client-controller-gamma"))
                .andExpect(jsonPath("$.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.content[0].apikey").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void unknownApiKeyFilterFieldReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queryRequest("apikey", "EQUALS", "30001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Query Request"))
                .andExpect(jsonPath("$.message").value("Unknown query field 'apikey'."));
    }

    @Test
    void unknownRolesFilterFieldReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queryRequest("roles", "EQUALS", "\"CLIENT\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Query Request"))
                .andExpect(jsonPath("$.message").value("Unknown query field 'roles'."));
    }

    @Test
    void unsupportedOperatorOnBooleanFieldReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queryRequest("enabled", "CONTAINS", "true")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Query Request"))
                .andExpect(jsonPath("$.message").value("Operator CONTAINS is not supported for field 'enabled'."));
    }

    @Test
    void invalidIdValueTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queryRequest("id", "EQUALS", "\"not-a-number\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Query Request"))
                .andExpect(jsonPath("$.message").value("Field 'id' requires a JSON number; received String."));
    }

    @Test
    void invalidRequestValidationReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"size\": 101 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("size: must be less than or equal to 100"));
    }

    @Test
    void malformedOperatorEnumReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/client/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queryRequest("email", "MATCHES", "\"example.com\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed Request"))
                .andExpect(jsonPath("$.message").value("Malformed request body."));
    }

    private String queryRequest(String field, String operator, String jsonValue) {
        return """
                {
                  "filters": [
                    {
                      "field": "%s",
                      "operations": [ { "operator": "%s", "value": %s } ]
                    }
                  ]
                }
                """.formatted(field, operator, jsonValue);
    }
}
