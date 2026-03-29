package by.lobacevich.auth.dto.response;

import by.lobacevich.auth.entity.enums.Role;

public record UserDtoResponse(String login,
                              Long userId,
                              Role role) {
}
