package org.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "alex")
    private String username;

    @Schema(example = "USER")
    private String role;
}
