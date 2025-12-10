package org.example.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/session-demo")
public class SessionDemoController {

    @GetMapping("/hit")
    public Map<String, Object> hit(HttpSession session) {
        Integer counter = (Integer) session.getAttribute("counter");
        if (counter == null) {
            counter = 0;
        }
        counter++;
        session.setAttribute("counter", counter);

        return Map.of(
                "sessionId", session.getId(),
                "hits", counter
        );
    }
}
