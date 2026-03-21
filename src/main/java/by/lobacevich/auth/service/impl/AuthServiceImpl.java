package by.lobacevich.auth.service.impl;

import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.RegisterRequestDto;
import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.dto.response.UserDtoResponse;
import by.lobacevich.auth.entity.Credential;
import by.lobacevich.auth.exception.EntityNotFoundException;
import by.lobacevich.auth.exception.InvalidDataException;
import by.lobacevich.auth.repository.CredentialRepository;
import by.lobacevich.auth.service.AuthService;
import by.lobacevich.auth.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final CredentialRepository repository;
    private final PasswordEncoder encoder;
    private final JwtTokenService tokenService;

    @Override
    public UserDtoResponse register(RegisterRequestDto dto) {
        if (repository.existsById(dto.userId())) {
            throw new InvalidDataException("User with id " + dto.userId() + " already exists");
        }
        if (repository.existsByLogin(dto.login())) {
            throw new InvalidDataException("User with login " + dto.login() + " already exists");
        }

        Credential credential = Credential.builder()
                .login(dto.login())
                .userId(dto.userId())
                .passwordHash(encoder.encode(dto.password()))
                .build();
        repository.save(credential);
        return new UserDtoResponse(credential.getLogin(),
                credential.getUserId(),
                credential.getRole());
    }

    @Override
    public TokenResponseDto login(LoginRequestDto dto) {
        Credential credential = repository.findByLogin(dto.login()).orElseThrow(() ->
                new EntityNotFoundException("User with login " + dto.login() + " not found"));
        if (encoder.matches(dto.password(), credential.getPasswordHash())) {
            String access = tokenService.generateToken(credential.getUserId(), credential.getRole());
            String refresh = tokenService.generateRefreshToken(credential.getUserId());
            return new TokenResponseDto(access, refresh);
        } else {
            throw new InvalidDataException("Incorrect password");
        }
    }

    @Override
    public TokenResponseDto refresh(TokenRequestDto tokenDto) {
        Claims claims = tokenService.parse(tokenDto.token());
        Long userId = Long.valueOf(claims.getSubject());
        Credential user = repository.findById(userId).orElseThrow((() ->
                new EntityNotFoundException("User with id " + userId + " not found")));
        return new TokenResponseDto(tokenService.generateToken(userId, user.getRole()),
                tokenService.generateRefreshToken(userId));
    }

    @Override
    public void validate(String token) {
        tokenService.parse(token);
    }
}
