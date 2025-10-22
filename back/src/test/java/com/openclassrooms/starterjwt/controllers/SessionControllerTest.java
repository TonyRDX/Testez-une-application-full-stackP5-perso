package com.openclassrooms.starterjwt.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.dto.SessionDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SessionControllerTest {
    @Autowired MockMvc mvc;
    @Autowired private SessionMapper sessionMapper;
    @Autowired private SessionService sessionService;
    @Autowired private ObjectMapper objectMapper;

    @MockBean 
    private SessionRepository sessionRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById() throws Exception {
        Long id = 1L;
        Session session = new Session();
        session.setId(id);
        session.setDate(Date.from(Instant.now()));
        session.setDescription("desc");
        when(sessionRepository.findAll()).thenReturn(List.of(session));

        mvc.perform(get("/api/session"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(id));
    }

    private Session buildSession(Long id) {
        return buildSession(id , 1L);
    }
    
    private Session buildSession(Long id, Long userId) {
        User u = new User();
        u.setId(userId);
        List<User> userList = new ArrayList<>();
        userList.add(u);

        Session s = new Session();
        s.setId(id);
        s.setName("Yoga");
        s.setDescription("desc");
        s.setUsers(userList);
        s.setDate(Date.from(Instant.parse("2025-01-01T10:00:00Z")));
        return s;
    }

    private SessionDto buildSessionDto(Long id) {
        SessionDto dto = new SessionDto();
        dto.setId(id);
        dto.setName("Yoga");
        dto.setDescription("desc");
        dto.setDate(Date.from(Instant.parse("2025-01-01T10:00:00Z")));
        dto.setTeacher_id(42L);
        dto.setUsers(List.of());
        return dto;
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_ok() throws Exception {
        Session s = buildSession(5L);
        when(sessionRepository.findById(5L)).thenReturn(Optional.of(s));

        mvc.perform(get("/api/session/{id}", "5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(5L))
            .andExpect(jsonPath("$.name").value("Yoga"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_notFound() throws Exception {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/session/{id}", "99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_badRequest() throws Exception {
        mvc.perform(get("/api/session/{id}", "abc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testCreate_ok() throws Exception {
        SessionDto input = buildSessionDto(null);
        Session toSave = sessionMapper.toEntity(input);
        Session saved = buildSession(10L);

        when(sessionRepository.save(toSave)).thenReturn(saved);

        mvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.name").value("Yoga"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testUpdate_ok() throws Exception {
        SessionDto input = buildSessionDto(null);
        Session incoming = sessionMapper.toEntity(input);

        Session existing = buildSession(7L);
        Session updated = buildSession(7L);
        updated.setDescription("desc updated");

        when(sessionRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(sessionRepository.save(existing)).thenReturn(updated);

        mvc.perform(put("/api/session/{id}", "7")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7L))
            .andExpect(jsonPath("$.description").value("desc updated"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testUpdate_badRequest_invalidId() throws Exception {
        SessionDto input = buildSessionDto(null);

        mvc.perform(put("/api/session/{id}", "oops")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testDelete_ok() throws Exception {
        when(sessionRepository.findById(3L)).thenReturn(Optional.of(buildSession(3L)));
        doNothing().when(sessionRepository).deleteById(3L);

        mvc.perform(delete("/api/session/{id}", "3"))
            .andExpect(status().isOk());

        verify(sessionRepository, times(1)).deleteById(3L);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testDelete_notFound() throws Exception {
        when(sessionRepository.findById(123L)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/session/{id}", "123"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testDelete_badRequest_invalidId() throws Exception {
        mvc.perform(delete("/api/session/{id}", "NaN"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testParticipate_ok() throws Exception {
        Session session = buildSession(11L);
        User user = new User();
        user.setId(77L);

        when(sessionRepository.findById(11L)).thenReturn(Optional.of(session));
        when(userRepository.findById(77L)).thenReturn(Optional.of(user));
        when(sessionRepository.save(session)).thenReturn(session);

        mvc.perform(post("/api/session/{id}/participate/{userId}", "11", "77"))
            .andExpect(status().isOk());

        assertTrue(
            session.getUsers().stream().anyMatch(u -> u.getId().equals(77L)),
            "L'utilisateur 77 doit être ajouté à la session"
        );
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testParticipate_badRequest_invalidIds() throws Exception {
        mvc.perform(post("/api/session/{id}/participate/{userId}", "x", "y"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
        public void testParticipate_badRequest_userAlreadyParticipant() throws Exception {
        Session session = buildSession(11L, 77L);
        User user = new User(); user.setId(77L);

        when(sessionRepository.findById(11L)).thenReturn(Optional.of(session));
        when(userRepository.findById(77L)).thenReturn(Optional.of(user));

        mvc.perform(post("/api/session/{id}/participate/{userId}", "11", "77"))
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testParticipate_notFound_sessionMissing() throws Exception {
        when(sessionRepository.findById(11L)).thenReturn(Optional.empty());
        when(userRepository.findById(77L)).thenReturn(Optional.of(new User()));

        mvc.perform(post("/api/session/{id}/participate/{userId}", "11", "77"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testNoLongerParticipate_ok() throws Exception {
        Session session = buildSession(12L, 88L);

        when(sessionRepository.findById(12L)).thenReturn(java.util.Optional.of(session));
        when(sessionRepository.save(session)).thenReturn(session);

        mvc.perform(delete("/api/session/{id}/participate/{userId}", "12", "88"))
                .andExpect(status().isOk());

        assertTrue(
                session.getUsers().stream().noneMatch(u -> u.getId().equals(88L)),
                "L'utilisateur 88 doit être retiré de la session"
        );
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testNoLongerParticipate_badRequest_invalidIds() throws Exception {
        mvc.perform(delete("/api/session/{id}/participate/{userId}", "bad", "id"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testParticipate_notFound_userMissing() throws Exception {
        when(sessionRepository.findById(11L)).thenReturn(Optional.of(buildSession(11L)));
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        mvc.perform(post("/api/session/{id}/participate/{userId}", "11", "77"))
        .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testNoLongerParticipate_badRequest_userNotParticipant() throws Exception {
        // Session sans l'utilisateur 88
        Session session = buildSession(12L);
        session.setUsers(new ArrayList<>()); // vide

        when(sessionRepository.findById(12L)).thenReturn(Optional.of(session));

        mvc.perform(delete("/api/session/{id}/participate/{userId}", "12", "88"))
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testNoLongerParticipate_notFound_sessionMissing() throws Exception {
        when(sessionRepository.findById(12L)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/session/{id}/participate/{userId}", "12", "88"))
        .andExpect(status().isNotFound());
    }
}
