package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.user.AppUser;
import org.example.user.AppUserRepository;
import org.example.user.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(AppUserRepository repository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            if (!repository.existsByUsername("admin")) {
                AppUser admin = AppUser.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .role(Role.ADMIN)
                        .build();

                repository.save(admin);
            }
        };
    }
}
