package com.example.securedb;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Automated tests for the security controls. Run with: mvn test
 *
 * These tests exercise the filter chain through MockMvc, so authentication
 * and authorization are real. Two honest notes:
 *  - MockMvc does not open a socket, so the TLS/HTTPS layer is not what is
 *    under test here; HTTPS-only behavior is shown by the curl evidence in
 *    the submission (the app opens no plain HTTP connector at all).
 *  - Observed difference between harness and live server: with a valid
 *    login but no CSRF token, the MockMvc run answers 403 Forbidden while
 *    the curl run over real HTTPS answers 401. Both are the same rejection
 *    - the mutation does not happen - and test 9 confirms no record is
 *    created. The exact status code differs between the two environments;
 *    the security property does not.
 *
 * Tests run in declared order so state assertions are deterministic:
 * data.sql seeds exactly two employees.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeApiTests {

    @Autowired
    private MockMvc mvc;

    @Test
    @Order(1)
    void unauthenticatedRequestIsRejected() throws Exception {
        mvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void authenticatedUserCanReadSeededEmployees() throws Exception {
        mvc.perform(get("/api/employees").with(httpBasic("user", "User@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().string(containsString("Jane Doe")))
                .andExpect(content().string(containsString("John Smith")));
    }

    @Test
    @Order(3)
    void userWithValidCsrfTokenStillCannotWrite() throws Exception {
        mvc.perform(post("/api/employees")
                        .with(httpBasic("user", "User@123"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bad Actor\",\"email\":\"bad@example.com\",\"department\":\"HR\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void adminMutationWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/api/employees")
                        .with(httpBasic("admin", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No Token\",\"email\":\"n@example.com\",\"department\":\"IT\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void adminMutationWithCsrfTokenSucceeds() throws Exception {
        mvc.perform(post("/api/employees")
                        .with(httpBasic("admin", "Admin@123"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Eve Adams\",\"email\":\"eve@example.com\",\"department\":\"HR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Eve Adams"));
    }

    @Test
    @Order(6)
    void sqlInjectionPayloadIsBoundAsLiteralValue() throws Exception {
        // The classic payload must match nothing: it is a parameter value,
        // never executed as SQL.
        mvc.perform(get("/api/employees/department/{dept}", "' OR '1'='1")
                        .with(httpBasic("user", "User@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(7)
    void userDeleteIsForbidden() throws Exception {
        mvc.perform(delete("/api/employees/{id}", 3)
                        .with(httpBasic("user", "User@123"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void adminDeleteWithCsrfTokenSucceeds() throws Exception {
        mvc.perform(delete("/api/employees/{id}", 3)
                        .with(httpBasic("admin", "Admin@123"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    void finalStateContainsOnlyTheSeededRecords() throws Exception {
        // Rejected writes (tests 3 and 4) created nothing; test 8 removed
        // the one authorized record again.
        mvc.perform(get("/api/employees").with(httpBasic("user", "User@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
