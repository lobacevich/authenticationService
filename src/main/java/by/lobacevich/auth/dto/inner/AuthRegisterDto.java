package by.lobacevich.auth.dto.inner;

public record AuthRegisterDto(String login,
                              String password,
                              Long userId) {
}
