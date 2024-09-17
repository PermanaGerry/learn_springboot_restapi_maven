package com.maven.restapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maven.restapi.dto.AddressResponse;
import com.maven.restapi.dto.CreateAddressRequest;
import com.maven.restapi.dto.UpdateAddressRequest;
import com.maven.restapi.dto.WebResponse;
import com.maven.restapi.models.entity.Address;
import com.maven.restapi.models.entity.Contact;
import com.maven.restapi.models.entity.User;
import com.maven.restapi.models.repository.AddressRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Contact contact;
    private Address address;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();

        // fake data user
        user = new User();
        user.setUsername("example@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setName("example");
        user.setToken("exampleToken");
        user.setTokenExpiredAt(System.currentTimeMillis() + (1000 * 16 * 24 * 30));
        userRepository.save(user);

        // fake data contact
        contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);
        contact.setFirstName("Gerry");
        contact.setLastName("Putra");
        contact.setEmail("example@test.com");
        contact.setPhone("09875372518237");

        // fake data address
        address = new Address();
        address.setId(UUID.randomUUID().toString());
        address.setContact(contact);
        address.setStreet("Jl Imam Sadiki no 7");
        address.setCity("Kota Malang");
        address.setProvince("Jawa Timur");
        address.setCountry("Indonesia");
        address.setPostalCode("456241");
    }

    @Test
    void createAddressNotFoundContact() throws Exception{
        contactRepository.save(contact);

        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet(address.getStreet());
        request.setCity(address.getCity());
        request.setProvince(address.getProvince());
        request.setCountry(address.getCountry());
        request.setPostalCode(address.getPostalCode());

        mockMvc.perform(
                post("/api/contacts/not-found/addresses")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Contact is not found.", response.getErrors());
        });
    }

    @Test
    void createAddressBlankValue() throws Exception{
        contactRepository.save(contact);

        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet(address.getStreet());
        request.setCity(address.getCity());
        request.setProvince(address.getProvince());
        request.setCountry("");
        request.setPostalCode(address.getPostalCode());

        mockMvc.perform(
                post("/api/contacts/" + contact.getId() + "/addresses")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("country: must not be blank", response.getErrors());
        });
    }

    @Test
    void createAddressSuccess() throws Exception{
        contactRepository.save(contact);

        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet(address.getStreet());
        request.setCity(address.getCity());
        request.setProvince(address.getProvince());
        request.setCountry(address.getCountry());
        request.setPostalCode(address.getPostalCode());

        mockMvc.perform(
                post("/api/contacts/" + contact.getId() + "/addresses")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
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
    void getAddressNotFound() throws Exception{
        contactRepository.save(contact);

        addressRepository.save(address);

        mockMvc.perform(
                get("/api/contacts/" + contact.getId() + "/addresses/not-found")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Address is not found.", response.getErrors());
        });
    }

    @Test
    void getAddressNotFoundContact() throws Exception{
        contactRepository.save(contact);

        addressRepository.save(address);

        mockMvc.perform(
                get("/api/contacts/not-found/addresses/"+address.getId())
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Contact is not found.", response.getErrors());
        });
    }

    @Test
    void getAddressSuccess() throws Exception{
        contactRepository.save(contact);
        addressRepository.save(address);

        mockMvc.perform(
                get("/api/contacts/"+contact.getId()+"/addresses/"+address.getId())
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(address.getId(), response.getData().getId());
            assertEquals(address.getStreet(), response.getData().getStreet());
            assertEquals(address.getCity(), response.getData().getCity());
            assertEquals(address.getProvince(), response.getData().getProvince());
            assertEquals(address.getCountry(), response.getData().getCountry());
            assertEquals(address.getPostalCode(), response.getData().getPostalCode());

            Address addressDb = addressRepository.findFirstByContactAndId(contact, address.getId())
                    .orElse(null);
            assertEquals(address.getId(), addressDb.getId());
            assertEquals(address.getStreet(), addressDb.getStreet());
            assertEquals(address.getCity(), addressDb.getCity());
            assertEquals(address.getProvince(), addressDb.getProvince());
            assertEquals(address.getCountry(), addressDb.getCountry());
            assertEquals(address.getPostalCode(), addressDb.getPostalCode());
        });
    }

    @Test
    void updateAddressNotFoundContact() throws Exception{
        contactRepository.save(contact);

        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet(address.getStreet());
        request.setCity(address.getCity());
        request.setProvince(address.getProvince());
        request.setCountry(address.getCountry());
        request.setPostalCode(address.getPostalCode());

        mockMvc.perform(
                put("/api/contacts/not-found/addresses/"+address.getId())
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Contact is not found.", response.getErrors());
        });
    }

    @Test
    void updateAddressNotFound() throws Exception{
        contactRepository.save(contact);

        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet(address.getStreet());
        request.setCity(address.getCity());
        request.setProvince(address.getProvince());
        request.setCountry(address.getCountry());
        request.setPostalCode(address.getPostalCode());

        mockMvc.perform(
                put("/api/contacts/"+contact.getId()+"/addresses/not-found")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Address is not found.", response.getErrors());
        });
    }

    @Test
    void updateAddressValueBlank() throws Exception{
        contactRepository.save(contact);
        addressRepository.save(address);

        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet(address.getStreet());
        request.setCity(address.getCity());
        request.setProvince(address.getProvince());
        request.setPostalCode(address.getPostalCode());

        mockMvc.perform(
                put("/api/contacts/"+contact.getId()+"/addresses/"+address.getId())
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("country: must not be blank", response.getErrors());
        });
    }

    @Test
    void updateAddressSuccess() throws Exception{
        contactRepository.save(contact);
        addressRepository.save(address);

        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet("JL Test Update Street");
        request.setCity("Test City");
        request.setProvince("Test Province");
        request.setCountry("Test Country");
        request.setPostalCode("26");

        mockMvc.perform(
                put("/api/contacts/"+contact.getId()+"/addresses/"+address.getId())
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(address.getId(), response.getData().getId());
            assertEquals(request.getStreet(), response.getData().getStreet());
            assertEquals(request.getCity(), response.getData().getCity());
            assertEquals(request.getProvince(), response.getData().getProvince());
            assertEquals(request.getCountry(), response.getData().getCountry());
            assertEquals(request.getPostalCode(), response.getData().getPostalCode());

            Address addressDb = addressRepository.findFirstByContactAndId(contact, address.getId())
                    .orElse(null);
            assertEquals(addressDb.getId(), response.getData().getId());
            assertEquals(addressDb.getStreet(), response.getData().getStreet());
            assertEquals(addressDb.getCity(), response.getData().getCity());
            assertEquals(addressDb.getProvince(), response.getData().getProvince());
            assertEquals(addressDb.getCountry(), response.getData().getCountry());
            assertEquals(addressDb.getPostalCode(), response.getData().getPostalCode());
        });
    }

    @Test
    void deleteAddressNotFound() throws Exception{
        contactRepository.save(contact);
        addressRepository.save(address);

        mockMvc.perform(
                delete("/api/contacts/" + contact.getId() + "/addresses/not-found")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Address is not found.", response.getErrors());
        });
    }

    @Test
    void deleteAddressNotFoundContact() throws Exception{
        contactRepository.save(contact);
        addressRepository.save(address);

        mockMvc.perform(
                delete("/api/contacts/not-found/addresses/"+address.getId())
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Contact is not found.", response.getErrors());
        });
    }

    @Test
    void deleteAddressSuccess() throws Exception{
        contactRepository.save(contact);
        addressRepository.save(address);

        mockMvc.perform(
                delete("/api/contacts/"+contact.getId()+"/addresses/"+address.getId())
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals("Ok", response.getData());
            assertFalse(addressRepository.existsById(address.getId()));
        });
    }

    @Test
    void listAddressNotFoundContact() throws Exception{
        contactRepository.save(contact);

        mockMvc.perform(
                get("/api/contacts/not-found/addresses")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getData());
            assertNotNull(response.getErrors());
            assertEquals("Contact is not found.", response.getErrors());
        });
    }


    @Test
    void listAddressesNotDataSuccess() throws Exception {
        contactRepository.save(contact);

        mockMvc.perform(
                get("/api/contacts/"+contact.getId()+"/addresses")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<AddressResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(0, response.getData().size());
        });
    }

    @Test
    void listAddressesSuccess() throws Exception {
        contactRepository.save(contact);

        for (int i = 0; i < 100; i++) {
            address.setId(UUID.randomUUID().toString());
            address.setContact(contact);
            address.setStreet("Jl Imam Sadiki no 7");
            address.setCity("Kota Malang");
            address.setProvince("Jawa Timur");
            address.setCountry("Indonesia");
            address.setPostalCode("456241");

            addressRepository.save(address);
        }

        mockMvc.perform(
                get("/api/contacts/"+contact.getId()+"/addresses")
                        .header("X-API-Token", user.getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<AddressResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData());
            assertEquals(100, response.getData().size());
        });
    }
}
