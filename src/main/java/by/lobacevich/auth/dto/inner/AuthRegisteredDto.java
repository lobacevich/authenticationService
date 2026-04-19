package by.lobacevich.auth.dto.inner;

import by.lobacevich.auth.entity.enums.Role;

public record AuthRegisteredDto(String login,
                                Role role) {
}
