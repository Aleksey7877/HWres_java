package org.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Schema(example = "new_login")
    @Size(min = 3, max = 50)
    private String username;

    @Schema(example = "newPassword123")
    @Size(min = 4, max = 100)
    private String password;

    @Schema(example = "ADMIN")
    private String role;  // "USER" или "ADMIN"
}
