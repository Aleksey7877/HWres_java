package org.example.user;

import org.example.user.dto.UpdateUserRequest;
import org.example.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;


    @Test
    void getAll_returnsMappedResponses() {
        AppUser u1 = new AppUser();
        u1.setId(1L);
        u1.setUsername("admin");
        u1.setRole(Role.ADMIN);

        AppUser u2 = new AppUser();
        u2.setId(2L);
        u2.setUsername("user1");
        u2.setRole(Role.USER);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> result = userController.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
        assertThat(result.get(0).getRole()).isEqualTo("ADMIN");
        assertThat(result.get(1).getUsername()).isEqualTo("user1");
        assertThat(result.get(1).getRole()).isEqualTo("USER");

        verify(userRepository).findAll();
    }


    @Test
    void getById_returnsUserResponse() {
        AppUser user = new AppUser();
        user.setId(10L);
        user.setUsername("alex");
        user.setRole(Role.USER);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        UserResponse result = userController.getById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("alex");
        assertThat(result.getRole()).isEqualTo("USER");

        verify(userRepository).findById(10L);
    }

    @Test
    void getById_whenNotFound_throws404() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userController.getById(99L)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository).findById(99L);
    }


    @Test
    void update_changesUsernamePasswordRole_andSaves() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("old_login");
        user.setPassword("old_pass");
        user.setRole(Role.USER);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("new_login");
        req.setPassword("secret123");
        req.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new_login")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_pass");
        when(userRepository.save(user)).thenReturn(user);

        UserResponse result = userController.update(1L, req);

        assertThat(result.getUsername()).isEqualTo("new_login");
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(user.getPassword()).isEqualTo("encoded_pass");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("new_login");
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(user);
    }

    @Test
    void update_whenUsernameAlreadyExists_throwsBadRequest() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("old");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("taken");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userController.update(1L, req)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenRoleInvalid_throwsBadRequest() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alex");
        user.setRole(Role.USER);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setRole("SUPERADMIN"); // некорректное значение

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userController.update(1L, req)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }


    @Test
    void delete_whenExists_deletes() {
        when(userRepository.existsById(5L)).thenReturn(true);

        userController.delete(5L);

        verify(userRepository).existsById(5L);
        verify(userRepository).deleteById(5L);
    }

    @Test
    void delete_whenNotExists_throws404() {
        when(userRepository.existsById(5L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userController.delete(5L)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository).existsById(5L);
        verify(userRepository, never()).deleteById(anyLong());
    }
    @Test
    void update_whenUsernameSameAndNoOtherFields_savesWithoutExtraChanges() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("same");
        user.setPassword("old_pass");
        user.setRole(Role.USER);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("same");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse result = userController.update(1L, req);

        assertThat(result.getUsername()).isEqualTo("same");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(user.getPassword()).isEqualTo("old_pass");

        verify(userRepository, never()).existsByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(user);
    }
    @Test
    void update_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("new");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userController.update(404L, req)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository).findById(404L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenPasswordBlank_doesNotEncodeOrSaveIt() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alex");
        user.setPassword("old_pass");
        user.setRole(Role.USER);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setPassword("   ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse result = userController.update(1L, req);

        assertThat(result.getUsername()).isEqualTo("alex");
        assertThat(user.getPassword()).isEqualTo("old_pass");

        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void update_whenRoleBlank_doesNotChangeRole() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alex");
        user.setRole(Role.USER);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setRole("   ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse result = userController.update(1L, req);

        assertThat(result.getRole()).isEqualTo("USER");
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

}
