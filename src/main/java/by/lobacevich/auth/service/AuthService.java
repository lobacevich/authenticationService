package by.lobacevich.auth.service;

import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.RegisterRequestDto;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.dto.response.UserDtoResponse;

public interface AuthService {

    UserDtoResponse register(RegisterRequestDto dto);

    TokenResponseDto login(LoginRequestDto dto);

    TokenResponseDto refresh(TokenRequestDto token);

    void validate(String token);
}
