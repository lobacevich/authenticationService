package by.lobacevich.auth.dto.request;

public record UserCreateRequestDto(String name,
                                   String surname,
                                   String birthDate,
                                   String email) {
}
