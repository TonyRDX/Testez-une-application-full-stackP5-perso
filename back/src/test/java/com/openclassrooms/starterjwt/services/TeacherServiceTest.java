package com.openclassrooms.starterjwt.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;

@SpringBootTest
public class TeacherServiceTest {
    @MockBean
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherService teacherService;

    @Test
    public void testFindAll() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Tony");
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));

        List<Teacher> result = teacherService.findAll();

        assertNotNull(result);
        assertEquals(result.get(0).getId(), 1L);
        assertEquals(result.get(0).getFirstName(), "Tony");
    }

    @Test
    public void testFindById() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Tony");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        Teacher result = teacherService.findById(1L);

        assertNotNull(result);
        assertEquals(result.getId(), 1L);
    }
}
