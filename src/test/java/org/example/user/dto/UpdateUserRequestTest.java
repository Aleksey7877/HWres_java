package org.example.user.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateUserRequestTest {

    @Test
    void gettersAndSetters_work() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("new_login");
        req.setPassword("new_pwd");
        req.setRole("ADMIN");

        assertThat(req.getUsername()).isEqualTo("new_login");
        assertThat(req.getPassword()).isEqualTo("new_pwd");
        assertThat(req.getRole()).isEqualTo("ADMIN");
    }
}
