package org.example.user.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void allArgsConstructorAndGetters_work() {
        UserResponse resp = new UserResponse(1L, "alex", "ADMIN");

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getUsername()).isEqualTo("alex");
        assertThat(resp.getRole()).isEqualTo("ADMIN");
    }
}
