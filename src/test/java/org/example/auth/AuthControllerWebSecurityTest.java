package org.example.auth;

import org.example.config.SecurityConfig;
import org.example.user.AppUser;
import org.example.user.AppUserRepository;
import org.example.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, AuthControllerWebSecurityTest.TestUsersConfig.class})
class AuthControllerWebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserRepository userRepository;

    @TestConfiguration
    static class TestUsersConfig {
        @Bean
        UserDetailsService userDetailsService(PasswordEncoder encoder) {
            return new InMemoryUserDetailsManager(
                    User.withUsername("admin").password(encoder.encode("admin")).roles("ADMIN").build(),
                    User.withUsername("user1").password(encoder.encode("pass1")).roles("USER").build()
            );
        }
    }

    @Test
    void register_anonymous_allowed_permitAll() throws Exception {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        AppUser saved = AppUser.builder()
                .id(1L)
                .username("newuser")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(AppUser.class))).thenReturn(saved);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void me_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository);
    }

    @Test
    void me_authenticated_returnsUser() throws Exception {
        AppUser u = AppUser.builder()
                .id(42L)
                .username("user1")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername(eq("user1"))).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/auth/me")
                        .with(httpBasic("user1", "pass1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userRepository).findByUsername("user1");
    }
}