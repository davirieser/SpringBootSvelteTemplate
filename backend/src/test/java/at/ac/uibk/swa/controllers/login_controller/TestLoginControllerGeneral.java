package at.ac.uibk.swa.controllers.login_controller;

import at.ac.uibk.swa.models.Permission;
import at.ac.uibk.swa.models.Person;
import at.ac.uibk.swa.service.PersonService;
import at.ac.uibk.swa.util.AuthGenerator;
import at.ac.uibk.swa.util.EndpointMatcherUtil;
import at.ac.uibk.swa.util.StringGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TestLoginControllerGeneral {
    @Autowired
    private PersonService personService;
    @Autowired
    private EndpointMatcherUtil endpointMatcherUtil;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;


    @Test
    public void login() throws Exception {
        // given: user created in database
        String username = StringGenerator.username();
        String password = StringGenerator.password();
        Set<GrantedAuthority> permissions = Set.of(Permission.USER, Permission.ADMIN);
        personService.create(new Person(username, StringGenerator.email(), password, permissions));

        // when: logging in as that user
        mockMvc.perform(MockMvcRequestBuilders
                        .post(endpointMatcherUtil.toApiEndpoint(endpointMatcherUtil.getApiLoginEndpoint()))
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_JSON)
                )
        // then: status code 200 must be returned, token must be in body and correct permissions must be returned
        .andExpectAll(
                status().isOk(),
                jsonPath("$.token").exists(),
                jsonPath("$.permissions").isArray(),
                jsonPath("$.permissions[*]").value(
                        Matchers.hasItems(
                            Permission.USER.toString(),
                            Permission.ADMIN.toString()
                        )
                )
        );
    }

    @Test
    public void loginWrongPassword() throws Exception {
        // given: user created in database
        String username = StringGenerator.username();
        String password = StringGenerator.password();
        Set<GrantedAuthority> permissions = Set.of(Permission.USER);
        personService.create(new Person(username, StringGenerator.email(), password, permissions));

        // when: trying to log in as that user with wrong password
        mockMvc.perform(MockMvcRequestBuilders
                        .post(endpointMatcherUtil.getApiLoginEndpoint())
                        .param("username", username)
                        .param("password", "wrong-password")
                        .contentType(MediaType.APPLICATION_JSON)
                )
        // then: status code 401 must be returned, token must not be contained in body
        .andExpectAll(
                status().isUnauthorized(),
                jsonPath("$.token").doesNotExist()
        );
    }

    @Test
    public void loginRandomCredentials() throws Exception {
        // given: default setup

        // when: trying to log in as that user with wrong password
        mockMvc.perform(MockMvcRequestBuilders
                        .post(endpointMatcherUtil.getApiLoginEndpoint())
                        .param("username", StringGenerator.username())
                        .param("password", StringGenerator.password())
                        .contentType(MediaType.APPLICATION_JSON)
                )
        // then: status code 401 must be returned, token must not be contained in body
        .andExpectAll(
                status().isUnauthorized(),
                jsonPath("$.token").doesNotExist()
        );
    }

    @Test
    public void logout() throws Exception {
        // given: a logged in user with a token
        String username = StringGenerator.username();
        String password = StringGenerator.password();
        Set<GrantedAuthority> permissions = Set.of(Permission.USER);
        Person person = new Person(username, StringGenerator.email(), password, permissions);
        assertTrue(personService.create(person), "Unable to create user");
        Optional<Person> maybePerson = personService.login(username, password);
        assertTrue(maybePerson.isPresent(), "Unable to login");

        // when: logging out that user
        mockMvc.perform(MockMvcRequestBuilders
                .post(endpointMatcherUtil.toApiEndpoint(endpointMatcherUtil.getApiLogoutEndpoint()))
                .header(HttpHeaders.AUTHORIZATION, AuthGenerator.generateToken(maybePerson.get()))
                .contentType(MediaType.APPLICATION_JSON)
        )
        // then: status code 200 must be returned
        .andExpectAll(
                status().isOk()
        );
    }

    @Test
    public void testLogoutWithRandomToken() throws Exception {
        // given: default setting

        // when: logging out with random token
        mockMvc.perform(MockMvcRequestBuilders
                        .post(endpointMatcherUtil.getApiLogoutEndpoint())
                        .header(HttpHeaders.AUTHORIZATION, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                )
        // then: status code 401 must be returned
        .andExpectAll(
                status().isUnauthorized()
        );
    }

    @Test
    public void testLogoutWithoutAnyToken() throws Exception {
        // given: default setting

        // when: logging out without token
        mockMvc.perform(MockMvcRequestBuilders
                        .post(endpointMatcherUtil.getApiLogoutEndpoint())
                        .contentType(MediaType.APPLICATION_JSON)
                )
        // then: status code 401 must be returned
        .andExpectAll(
                status().isUnauthorized()
        );
    }
}