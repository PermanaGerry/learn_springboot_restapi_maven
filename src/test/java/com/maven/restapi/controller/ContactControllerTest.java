package com.maven.restapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maven.restapi.dto.ContactResponse;
import com.maven.restapi.dto.CreateContactRequest;
import com.maven.restapi.dto.UpdateContactRequest;
import com.maven.restapi.dto.WebResponse;
import com.maven.restapi.models.entity.Contact;
import com.maven.restapi.models.entity.User;
import com.maven.restapi.models.repository.ContactRepository;
import com.maven.restapi.models.repository.UserRepository;
import com.maven.restapi.security.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    private Contact contact;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() + (1000 * 16 * 24 * 30));
        userRepository.save(user);

        contact = new Contact();
        contact.setFirstName("Gerry");
        contact.setLastName("Putra");
        contact.setEmail("example@test.com");
        contact.setPhone("09875372518237");
    }

    @Test
    void createContactBadRequest() throws Exception {
        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName("");
        request.setEmail(contact.getEmail());

        mockMvc.perform(
                post("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
            });

            assertNotNull(response.getErrors());
            assertNull(response.getData());
        });
    }

    @Test
    void createContactSuccess() throws Exception {
        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName(contact.getFirstName());
        request.setLastName(contact.getLastName());
        request.setEmail(contact.getEmail());
        request.setPhone(contact.getPhone());

        mockMvc.perform(
                post("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(request.getFirstName(), response.getData().getFirstName());
            assertEquals(request.getLastName(), response.getData().getLastName());
            assertEquals(request.getEmail(), response.getData().getEmail());
            assertEquals(request.getPhone(), response.getData().getPhone());

            Contact contactDB = contactRepository.findFirstByUserAndId(user, response.getData().getId()).orElse(null);
            assertNotNull(contactDB);
            assertEquals(contactDB.getFirstName(), request.getFirstName());
            assertEquals(contactDB.getLastName(), request.getLastName());
            assertEquals(contactDB.getEmail(), request.getEmail());
            assertEquals(contactDB.getPhone(), request.getPhone());
        });
    }

    @Test
    void getContactNotFound() throws Exception {
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);

        contactRepository.save(contact);

        mockMvc.perform(
                get("/api/contacts/not-found")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertNull(response.getData());
        });
    }

    @Test
    void getContactSuccess() throws Exception {
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);

        contactRepository.save(contact);

        mockMvc.perform(
                get("/api/contacts/" + contact.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(contact.getFirstName(), response.getData().getFirstName());
            assertEquals(contact.getLastName(), response.getData().getLastName());
            assertEquals(contact.getEmail(), response.getData().getEmail());
            assertEquals(contact.getPhone(), response.getData().getPhone());
        });
    }

    @Test
    void updateContactNotFoundId() throws Exception {
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);
        contactRepository.save(contact);

        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("updateContact");

        mockMvc.perform(
                patch("/api/contacts/not-found")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Contact not found.", response.getErrors());
        });
    }

    @Test
    void updateContactFailedValue() throws Exception {
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);
        contactRepository.save(contact);

        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("updateContact");
        request.setEmail("email");

        mockMvc.perform(
                patch("/api/contacts/" + contact.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("email: must be a well-formed email address", response.getErrors());
        });
    }

    @Test
    void updateContactSuccess() throws Exception {
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);
        contactRepository.save(contact);

        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("updateContact");

        mockMvc.perform(
                patch("/api/contacts/" + contact.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(request.getFirstName(), response.getData().getFirstName());
            assertEquals(contact.getEmail(), response.getData().getEmail());
            assertNotEquals(contact.getEmail(), request.getEmail());
        });
    }

    @Test
    void deleteContactNotFound() throws Exception {
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);
        contactRepository.save(contact);

        mockMvc.perform(
                delete("/api/contacts/not-found")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Contact not found.", response.getErrors());
        });
    }

    @Test
    void deleteContactSuccess() throws Exception {
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);
        contactRepository.save(contact);

        mockMvc.perform(
                delete("/api/contacts/" + contact.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals("Ok", response.getData());
        });
    }

    @Test
    void searchContactNotFound() throws Exception {

        mockMvc.perform(
                get("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals(0, response.getData().size());
            assertEquals(0, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });
    }

    @Test
    void searchContactSuccess() throws Exception {

        for (int i = 0; i < 100; i++) {
            contact.setId(UUID.randomUUID().toString());
            contact.setFirstName("Gerry " + " " + i);
            contact.setLastName("Permana " + " " + i);
            contact.setUser(user);
            contactRepository.save(contact);
        }

        // test first name
        mockMvc.perform(
                get("/api/contacts")
                        .queryParam("name", "Gerry")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals(10, response.getData().size());
            assertEquals(10, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });

        // test last name
        mockMvc.perform(
                get("/api/contacts")
                        .queryParam("name", "Permana")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals(10, response.getData().size());
            assertEquals(10, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });

        // test email
        mockMvc.perform(
                get("/api/contacts")
                        .queryParam("email", "example")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals(10, response.getData().size());
            assertEquals(10, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });

        // test phone
        mockMvc.perform(
                get("/api/contacts")
                        .queryParam("phone", "518237")
                        .queryParam("page", "2")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals(10, response.getData().size());
            assertEquals(10, response.getPaging().getTotalPage());
            assertEquals(2, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });

        // test phone offer page limit
        mockMvc.perform(
                get("/api/contacts")
                        .queryParam("phone", "518237")
                        .queryParam("page", "10000")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals(0, response.getData().size());
            assertEquals(10, response.getPaging().getTotalPage());
            assertEquals(10000, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });
    }

}
