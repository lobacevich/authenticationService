package by.lobacevich.auth.dto.response;

public record JwtAccessPayLoadDto(String type,
                                  Long userId,
                                  String role) {
}
