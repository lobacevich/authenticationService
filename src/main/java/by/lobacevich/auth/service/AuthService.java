package by.lobacevich.auth.service;

import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.RegisterRequestDto;
import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.dto.response.UserDtoResponse;

/**
 * Service for user authentication and registration.
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param dto registration data (login, password, userId)
     * @return created user info (login, userId, role)
     * @throws InvalidDataException if a user with the given userId already exists
     */
    UserDtoResponse register(RegisterRequestDto dto);

    /**
     * Authenticates a user by login and password.
     *
     * @param dto credentials (login, password)
     * @return pair of tokens (access, refresh)
     * @throws EntityNotFoundException    if the user is not found
     * @throws IncorrectPasswordException if the password is incorrect
     */
    TokenResponseDto login(LoginRequestDto dto);

    /**
     * Refreshes the token pair using a refresh token.
     *
     * @param token request containing the refresh token
     * @return new token pair (access, refresh)
     * @throws RefreshTokenException   if the refresh token is invalid
     * @throws EntityNotFoundException if the user is not found
     */
    TokenResponseDto refresh(TokenRequestDto token);

    /**
     * Validates and decodes a token.
     *
     * @param token JWT string (access or refresh)
     * @return payload object (JwtAccessPayLoadDto or JwtRefreshPayLoadDto)
     * @throws InvalidDataException if the token type is not supported
     * @throws JwtException         if the token is invalid or expired
     */
    Object validate(String token);
}
