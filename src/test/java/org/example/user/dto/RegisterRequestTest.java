package org.example.user.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    @Test
    void gettersAndSetters_work() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alex");
        req.setPassword("pwd");

        assertThat(req.getUsername()).isEqualTo("alex");
        assertThat(req.getPassword()).isEqualTo("pwd");
    }
}
