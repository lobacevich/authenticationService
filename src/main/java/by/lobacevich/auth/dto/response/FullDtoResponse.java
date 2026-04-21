package by.lobacevich.auth.dto.response;

public record FullDtoResponse(Long id,
                              String login,
                              String role,
                              String name,
                              String surname,
                              String birthDate,
                              String email,
                              Boolean active,
                              String createdAt,
                              String updatedAt) {
}
