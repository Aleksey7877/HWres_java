package org.example.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_returnsUserDetails() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alex");
        user.setPassword("encoded_pass");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("alex"))
                .thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("alex");

        assertThat(details.getUsername()).isEqualTo("alex");
        assertThat(details.getPassword()).isEqualTo("encoded_pass");
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.isEnabled()).isTrue();

        verify(userRepository).findByUsername("alex");
    }

    @Test
    void loadUserByUsername_whenNotFound_throws() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing")
        );

        verify(userRepository).findByUsername("missing");
    }
}
