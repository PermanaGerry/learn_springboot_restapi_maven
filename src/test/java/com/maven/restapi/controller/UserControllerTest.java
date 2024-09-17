package com.maven.restapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maven.restapi.dto.RegisterUserRequest;
import com.maven.restapi.dto.UpdateUserRequest;
import com.maven.restapi.dto.UserResponse;
import com.maven.restapi.dto.WebResponse;
import com.maven.restapi.models.entity.User;
import com.maven.restapi.models.repository.UserRepository;
import com.maven.restapi.security.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private static final Logger log = LogManager.getLogger(UserControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUserSuccess() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("test@example.com");
        request.setPassword("password");
        request.setName("test");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals("OK", response.getData());
        });
    }

    @Test
    void testRegisterUserBadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();

        request.setUsername("");
        request.setPassword("");
        request.setName("");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testRegisterUserDuplicated() throws Exception {
        User user = new User();

        user.setUsername("test@exaple.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("test");
        userRepository.save(user);

        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("test@exaple.com");
        request.setPassword("password");
        request.setName("test");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("User name already register", response.getErrors());
        });
    }

    @Test
    void getUserUnauthorized() throws Exception {
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "notfound")
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
    void getUserUnauthorizedTokenNotSend() throws Exception {
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
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
    void getUserUnauthorizedTokenExpiredAt() throws Exception {
        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() - (1000 * 16 * 24 * 30));
        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
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
    void getUserSuccess() throws Exception {

        Long milliseconds = 1000L * 60 * 60 * 24 * 30;

        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() + milliseconds);

        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(user.getUsername(), response.getData().getUsername());
            assertEquals(user.getName(), response.getData().getName());

            User userDB = userRepository.findFirstByToken(user.getToken())
                    .orElse(null);

            assertNotNull(userDB);
            assertEquals(userDB.getUsername(), user.getUsername());
            assertEquals(userDB.getName(), user.getName());
            assertEquals(userDB.getToken(), user.getToken());
            assertEquals(userDB.getTokenExpiredAt(), user.getTokenExpiredAt());
        });
    }

    @Test
    void updateUserUnauthorized() throws Exception {
        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
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
    void updateUserFieldNameToLong() throws Exception {

        Long milliseconds = 1000L * 60 * 60 * 24 * 30;

        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() + milliseconds);
        userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("password1");
        request.setName("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque tempor erat id erat laoreet mollis. Nullam porttitor, sapien in pretium dictum, turpis nibh imperdiet mauris, pretium laoreet dui risus quis neque. Vestibulum vitae dapibus libero. Nulla magna sem, cursus eget neque ac, efficitur lacinia sem. Vestibulum vel felis quis est ullamcorper interdum. Quisque malesuada laoreet felis a posuere. Pellentesque a elit ac urna convallis euismod et eget justo. Pellentesque in lorem pulvinar, molestie leo a, facilisis turpis. Nulla vestibulum vitae neque a faucibus. Fusce eu commodo elit, eget tincidunt ligula. Etiam sit amet leo at arcu varius tempus a sit amet dolor. Morbi tempor rutrum ligula, in suscipit diam vulputate ac. Sed accumsan, arcu vitae pulvinar vestibulum, mi ante tristique.");

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("name: size must be between 0 and 100", response.getErrors());
        });
    }

    @Test
    void updateUserFieldPasswordToLong() throws Exception {

        Long milliseconds = 1000L * 60 * 60 * 24 * 30;

        User user = new User();
        user.setUsername("test@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("tokenApi");
        user.setTokenExpiredAt(System.currentTimeMillis() + milliseconds);
        userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque tempor erat id erat laoreet mollis. Nullam porttitor, sapien in pretium dictum, turpis nibh imperdiet mauris, pretium laoreet dui risus quis neque. Vestibulum vitae dapibus libero. Nulla magna sem, cursus eget neque ac, efficitur lacinia sem. Vestibulum vel felis quis est ullamcorper interdum. Quisque malesuada laoreet felis a posuere. Pellentesque a elit ac urna convallis euismod et eget justo. Pellentesque in lorem pulvinar, molestie leo a, facilisis turpis. Nulla vestibulum vitae neque a faucibus. Fusce eu commodo elit, eget tincidunt ligula. Etiam sit amet leo at arcu varius tempus a sit amet dolor. Morbi tempor rutrum ligula, in suscipit diam vulputate ac. Sed accumsan, arcu vitae pulvinar vestibulum, mi ante tristique.");
        request.setName("example1");

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("password: size must be between 0 and 100", response.getErrors());
        });
    }

    @Test
    void updateUserSuccess() throws Exception {
        Long milliseconds = 1000L * 60 * 60 * 24 * 30;

        User user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() + milliseconds);
        userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("password1");
        request.setName("example1");

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(request.getName(), response.getData().getName());
            assertEquals(user.getUsername(), response.getData().getUsername());

            User userDB = userRepository.findFirstByToken(user.getToken())
                            .orElse(null);
            assertEquals(userDB.getName(), request.getName());
            assertTrue(BCrypt.checkpw(request.getPassword(), userDB.getPassword()));
        });
    }
}