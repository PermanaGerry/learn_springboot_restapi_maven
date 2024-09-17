package com.maven.restapi.controller.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maven.restapi.dto.LoginUserRequest;
import com.maven.restapi.dto.TokenResponse;
import com.maven.restapi.dto.WebResponse;
import com.maven.restapi.models.entity.User;
import com.maven.restapi.models.repository.UserRepository;
import com.maven.restapi.security.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void LoginUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("password");
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("example@example.com");
        request.setPassword("password");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
             WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

             assertNull(response.getErrors());
             assertNotNull(response.getData().getToken());
             assertNotNull(response.getData().getExpiredAt());

            User userDB = userRepository.findById(request.getUsername()).orElse(null);
            assertNotNull(userDB);
            assertEquals(userDB.getToken(), response.getData().getToken());
            assertEquals(userDB.getTokenExpiredAt(), response.getData().getExpiredAt());

        });
    }

    @Test
    void loginUserNotFound() throws Exception {
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("test@example.com");
        request.setPassword("password");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))

        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void loginUserFailedWrongPassword() throws Exception {
        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("example@example.com");
        request.setPassword("password1");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Username or Password wrong", response.getErrors());
        });
    }

    @Test
    void loginUserFailedWrongUsername() throws Exception {
        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("test@example.com");
        request.setPassword("password");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Username or Password wrong", response.getErrors());
        });
    }

    @Test
    void logoutUserUnauthorized() throws Exception {
        Long milliseconds = 1000L * 60 * 60 * 24 * 30;

        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() + milliseconds);

        userRepository.save(user);

        mockMvc.perform(
                delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "tokenNotFound")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Unauthorized", response.getErrors());
        });
    }

    @Test
    void logoutUserSuccess() throws Exception{
        Long milliseconds = 1000L * 60 * 60 * 24 * 30;

        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() + milliseconds);

        userRepository.save(user);

        mockMvc.perform(
                delete( "/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals("OK", response.getData());
            assertNull(response.getErrors());
        });
    }

}
