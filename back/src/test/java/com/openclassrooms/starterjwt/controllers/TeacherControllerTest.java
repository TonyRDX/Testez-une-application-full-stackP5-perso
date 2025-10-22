package com.openclassrooms.starterjwt.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.services.TeacherService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TeacherService.class)
public class TeacherControllerTest {

    @Autowired private MockMvc mvc;

    @MockBean private TeacherRepository teacherRepository;
    @MockBean private TeacherMapper teacherMapper;

    // --------- helpers ---------

    private Teacher buildTeacher(Long id, String first, String last) {
        Teacher t = new Teacher();
        t.setId(id);
        t.setFirstName(first);
        t.setLastName(last);
        return t;
    }

    private TeacherDto buildTeacherDto(Long id, String first, String last) {
        TeacherDto dto = new TeacherDto();
        dto.setId(id);
        dto.setFirstName(first);
        dto.setLastName(last);
        return dto;
    }

    // --------- tests: GET /api/teacher/{id} ---------

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_ok() throws Exception {
        Teacher entity = buildTeacher(5L, "Ada", "Lovelace");
        TeacherDto dto   = buildTeacherDto(5L, "Ada", "Lovelace");

        when(teacherRepository.findById(5L)).thenReturn(java.util.Optional.of(entity));
        when(teacherMapper.toDto(entity)).thenReturn(dto);

        mvc.perform(get("/api/teacher/{id}", "5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(5L))
            .andExpect(jsonPath("$.firstName").value("Ada"))
            .andExpect(jsonPath("$.lastName").value("Lovelace"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_notFound() throws Exception {
        when(teacherRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        mvc.perform(get("/api/teacher/{id}", "99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindById_badRequest_invalidId() throws Exception {
        mvc.perform(get("/api/teacher/{id}", "abc"))
            .andExpect(status().isBadRequest());
    }

    // --------- tests: GET /api/teacher ---------

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindAll_ok_multiple() throws Exception {
        List<Teacher> entities = Arrays.asList(
                buildTeacher(1L, "Alan", "Turing"),
                buildTeacher(2L, "Grace", "Hopper")
        );
        List<TeacherDto> dtos = Arrays.asList(
                buildTeacherDto(1L, "Alan", "Turing"),
                buildTeacherDto(2L, "Grace", "Hopper")
        );

        when(teacherRepository.findAll()).thenReturn(entities);
        when(teacherMapper.toDto(entities)).thenReturn(dtos);

        mvc.perform(get("/api/teacher"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].firstName").value("Alan"))
            .andExpect(jsonPath("$[0].lastName").value("Turing"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].firstName").value("Grace"))
            .andExpect(jsonPath("$[1].lastName").value("Hopper"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    public void testFindAll_ok_empty() throws Exception {
        when(teacherRepository.findAll()).thenReturn(Collections.<Teacher>emptyList());
        when(teacherMapper.toDto(Collections.<Teacher>emptyList()))
                .thenReturn(Collections.<TeacherDto>emptyList());

        mvc.perform(get("/api/teacher"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
    }
}
