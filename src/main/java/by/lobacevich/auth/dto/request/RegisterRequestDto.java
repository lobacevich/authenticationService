package by.lobacevich.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(@NotNull(message = "User id is required")
                                 Long userId,

                                 @NotBlank(message = "Login is required")
                                 @Size(min = 2, max = 63, message = "Login length must be between 3 and 63")
                                 String login,

                                 @NotBlank(message = "Password is required")
                                 @Size(min = 3, message = "Password length must be at least 3")
                                 String password) {
}
