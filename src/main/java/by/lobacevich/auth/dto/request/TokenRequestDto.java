package by.lobacevich.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequestDto(@NotBlank(message = "Token is empty")
                       String token) {
}
