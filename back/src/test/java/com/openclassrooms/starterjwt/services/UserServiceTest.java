package com.openclassrooms.starterjwt.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testDeleteUser() {
        Long id = 7L;

        userService.delete(id);

        verify(userRepository).deleteById(id);
        verifyNoMoreInteractions(userRepository); 
    }

    @Test
    public void testFindById() {
        Long id = 1L;
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(userRepository).findById(id);
    }

    @Test
    public void testFindByIdWhenNotFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.findById(id);

        assertNull(result);
        verify(userRepository, times(1)).findById(id);
        verifyNoMoreInteractions(userRepository);
    }
}
