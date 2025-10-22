package com.openclassrooms.starterjwt.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserRepository userRepository;
    @MockBean private UserMapper userMapper;

    // ---------- helpers ----------

    private User buildUser(Long id, String email, String first, String last, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFirstName(first);
        u.setLastName(last);
        u.setPassword("secret");
        u.setAdmin(admin);
        return u;
    }

    private UserDto buildUserDto(Long id, String email, String first, String last, boolean admin) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setEmail(email);
        dto.setFirstName(first);
        dto.setLastName(last);
        dto.setAdmin(admin);
        return dto;
    }

    // ---------- GET /api/user/{id} ----------

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_ok() throws Exception {
        User entity = buildUser(5L, "user@test.com", "John", "Doe", false);
        UserDto dto  = buildUserDto(5L, "user@test.com", "John", "Doe", false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(dto);

        mvc.perform(get("/api/user/{id}", "5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(5L))
            .andExpect(jsonPath("$.email").value("user@test.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_notFound() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/user/{id}", "99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_badRequest_invalidId() throws Exception {
        mvc.perform(get("/api/user/{id}", "abc"))
            .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /api/user/{id} ----------

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testDelete_ok_authorized() throws Exception {
        User entity = buildUser(7L, "user@test.com", "Jane", "Doe", false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(entity));

        mvc.perform(delete("/api/user/{id}", "7"))
            .andExpect(status().isOk());

        verify(userRepository, times(1)).deleteById(7L);
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = {"USER"})
    public void testDelete_unauthorized_differentUser() throws Exception {
        User entity = buildUser(8L, "user@test.com", "John", "Doe", false);
        when(userRepository.findById(8L)).thenReturn(Optional.of(entity));

        mvc.perform(delete("/api/user/{id}", "8"))
            .andExpect(status().isUnauthorized());

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testDelete_notFound() throws Exception {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/user/{id}", "123"))
            .andExpect(status().isNotFound());

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testDelete_badRequest_invalidId() throws Exception {
        mvc.perform(delete("/api/user/{id}", "NaN"))
            .andExpect(status().isBadRequest());

        verify(userRepository, never()).deleteById(anyLong());
    }
}
