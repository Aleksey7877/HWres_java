package org.example.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserTest {

    @Test
    void gettersAndSetters_workCorrectly() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alex");
        user.setPassword("secret");
        user.setRole(Role.ADMIN);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("alex");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }
}
