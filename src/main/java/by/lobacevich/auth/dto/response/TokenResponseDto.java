package by.lobacevich.auth.dto.response;

public record TokenResponseDto(String accessToken,
                               String refreshToken) {
}
