package com.conpresp.conprespapi.controller;

import com.conpresp.conprespapi.dto.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldCreateAUserAndReturnCreatedHTTPCodeAlongWithALocationHeader() throws Exception {
        var request = new UserRequest("Nask", "Nask@mail.com", "159");

        var response = makePostRequest(request)
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andReturn();

        var location = response.getResponse().getHeader("location");
        var uuid = location.substring(location.lastIndexOf("/") + 1);

        assertDoesNotThrow(() -> UUID.fromString(uuid));
    }

    @Test
    public void shouldReturnTheListOfUsers() throws Exception {
        var request1 = new UserRequest("Nask", "Nask@mail.com", "159");
        var request2 = new UserRequest("Matheus", "matheus@mail.com", "123");

        makePostRequest(request1);
        makePostRequest(request2);

        makeGetRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.users[0].name").value("Nask"))
                .andExpect(jsonPath("$.users[1].name").value("Matheus"));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        var request1 = new UserRequest("Nask", "Nask@mail.com", "159");
        var request2 = new UserRequest("Matheus", "matheus@mail.com", "123");

        makePostRequest(request1);

        var response = makePostRequest(request2)
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andReturn();

        var location = response.getResponse().getHeader("location");
        var uuid = location.substring(location.lastIndexOf("/") + 1);

        makeDeleteRequest(uuid);
        makeGetRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.users[0].name").value("Nask"));
    }

    @Test
    public void shouldReturnUserById() throws Exception {
        var request = new UserRequest("Matheus", "matheus@mail.com", "123");

        var response = makePostRequest(request)
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andReturn();

        var location = response.getResponse().getHeader("location");
        var uuid = location.substring(location.lastIndexOf("/") + 1);

        makeGetRequestById(uuid)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uuid))
                .andExpect(jsonPath("$.name").value("Matheus"));
    }

    @Test
    public void shouldReturnNotFoundByInvalidId() throws Exception {
        makeGetRequestById("abskajs")
                .andExpect(status().isNotFound());
    }

    private ResultActions makePostRequest(Object payload) throws Exception {
        return mockMvc
                .perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                );
    }

    private ResultActions makeGetRequest() throws Exception {
        return mockMvc
                .perform(get("/users").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions makeGetRequestById(String uuid) throws Exception {
        return mockMvc
                .perform(get("/users/" + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions makeDeleteRequest(String uuid) throws Exception {
        return mockMvc
                .perform(delete("/users/" + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }
}
