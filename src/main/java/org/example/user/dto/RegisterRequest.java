package org.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @Schema(example = "alex")
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Schema(example = "secret123")
    @NotBlank
    @Size(min = 4, max = 100)
    private String password;
}
