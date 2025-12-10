package org.example.session;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SessionDemoControllerTest {

    private final SessionDemoController controller = new SessionDemoController();

    @Test
    void hit_incrementsCounter() {
        HttpSession session = new MockHttpSession();

        Map<String, Object> first = controller.hit(session);
        Map<String, Object> second = controller.hit(session);

        assertThat(first.get("sessionId")).isEqualTo(session.getId());
        assertThat(first.get("hits")).isEqualTo(1);
        assertThat(second.get("hits")).isEqualTo(2);
    }
}
