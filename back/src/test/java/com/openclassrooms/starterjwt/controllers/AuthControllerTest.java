package com.openclassrooms.starterjwt.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private UserRepository userRepository;
    @MockBean private PasswordEncoder passwordEncoder; 

    // ---------- Helpers ----------

    private String loginJson(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return objectMapper.writeValueAsString(req);
    }

    private String registerJson(String email, String firstName, String lastName, String password) throws Exception {
        SignupRequest req = new SignupRequest();
        req.setEmail(email);
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setPassword(password);
        return objectMapper.writeValueAsString(req);
    }

    private User user(Long id, String email, String first, String last, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFirstName(first);
        u.setLastName(last);
        u.setPassword("secret");
        u.setAdmin(admin);
        return u;
    }

    private UserDetailsImpl principalFrom(User u) {
        // correspond exactement à ta classe UserDetailsImpl (builder Lombok)
        return UserDetailsImpl.builder()
                .id(u.getId())
                .username(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .admin(Boolean.valueOf(u.isAdmin()))
                .password(u.getPassword())
                .build();
    }

    private Authentication authenticated(UserDetailsImpl principal) {
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities() /* renvoie un HashSet<> vide */);
    }

    // ---------- Tests: /api/auth/login ----------

    @Test
    public void testLogin_ok_userNonAdmin() throws Exception {
        User u = user(1L, "user@test.com", "John", "Doe", false);
        UserDetailsImpl principal = principalFrom(u);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated(principal));
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt-token");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.<User>empty());

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson("user@test.com", "pwd")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.username").value("user@test.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    public void testLogin_ok_userAdmin_true() throws Exception {
        User admin = user(42L, "admin@test.com", "Ada", "Lovelace", true);
        UserDetailsImpl principal = principalFrom(admin);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated(principal));
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt-42");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson("admin@test.com", "pwd")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value("jwt-42"))
            .andExpect(jsonPath("$.id").value(42L))
            .andExpect(jsonPath("$.username").value("admin@test.com"))
            .andExpect(jsonPath("$.firstName").value("Ada"))
            .andExpect(jsonPath("$.lastName").value("Lovelace"))
            .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    public void testLogin_badCredentials_unauthorized() throws Exception {
        doThrow(new BadCredentialsException("Bad creds"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson("wrong@test.com", "badpwd")))
            .andExpect(status().isUnauthorized());
    }

    // ---------- Tests: /api/auth/register ----------

    @Test
    public void testRegister_ok() throws Exception {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd123")).thenReturn("hashed");

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("new@test.com", "Jane", "Doe", "pwd123")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    public void testRegister_emailAlreadyTaken_badRequest() throws Exception {
        when(userRepository.existsByEmail("exists@test.com")).thenReturn(true);

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("exists@test.com", "AAA", "BBB", "pwd123")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));
    }

    @Test
    public void testRegister_badRequest_invalidBody() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\",\"firstName\":\"\",\"lastName\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
}
