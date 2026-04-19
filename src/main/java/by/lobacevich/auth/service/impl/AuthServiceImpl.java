package by.lobacevich.auth.service.impl;

import by.lobacevich.auth.dto.inner.AuthRegisterDto;
import by.lobacevich.auth.dto.inner.AuthRegisteredDto;
import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.response.JwtAccessPayLoadDto;
import by.lobacevich.auth.dto.response.JwtRefreshPayLoadDto;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.entity.Credential;
import by.lobacevich.auth.entity.enums.TokenType;
import by.lobacevich.auth.exception.EntityNotFoundException;
import by.lobacevich.auth.exception.IncorrectPasswordException;
import by.lobacevich.auth.exception.InvalidDataException;
import by.lobacevich.auth.repository.CredentialRepository;
import by.lobacevich.auth.service.AuthService;
import by.lobacevich.auth.service.JwtTokenService;
import by.lobacevich.auth.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final CredentialRepository repository;
    private final PasswordEncoder encoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthRegisteredDto register(AuthRegisterDto dto) {
        if (repository.existsById(dto.userId())) {
            throw new InvalidDataException("User with id " + dto.userId() + " already exists");
        }

        Credential credential = Credential.builder()
                .login(dto.login())
                .userId(dto.userId())
                .passwordHash(encoder.encode(dto.password()))
                .build();
        repository.save(credential);
        return new AuthRegisteredDto(credential.getLogin(),
                credential.getRole());
    }

    @Override
    public TokenResponseDto login(LoginRequestDto dto) {
        Credential credential = repository.findByLogin(dto.login()).orElseThrow(() ->
                new EntityNotFoundException("User with login " + dto.login() + " not found"));
        if (encoder.matches(dto.password(), credential.getPasswordHash())) {
            String access = jwtTokenService.generateAccessToken(credential.getUserId(), credential.getRole());
            String refresh = jwtTokenService.generateRefreshToken(credential.getUserId());
            refreshTokenService.save(refresh, credential);
            return new TokenResponseDto(access, refresh);
        } else {
            throw new IncorrectPasswordException("Incorrect password");
        }
    }

    @Transactional
    @Override
    public TokenResponseDto refresh(TokenRequestDto tokenDto) {
        String oldRefreshToken = tokenDto.token();
        Claims claims = jwtTokenService.parse(oldRefreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        Credential user = repository.findById(userId).orElseThrow((() ->
                new EntityNotFoundException("User with id " + userId + " not found")));
        String newRefreshToken = jwtTokenService.generateRefreshToken(userId);
        refreshTokenService.refresh(oldRefreshToken, newRefreshToken);
        return new TokenResponseDto(jwtTokenService.generateAccessToken(userId, user.getRole()),
                newRefreshToken);
    }

    @Override
    public Object validate(String token) {
        Claims claims = jwtTokenService.parse(token);
        if (claims.get("type", String.class).equals(TokenType.ACCESS.name())) {
            return new JwtAccessPayLoadDto(TokenType.ACCESS.name(),
                    Long.parseLong(claims.getSubject()),
                    claims.get("role", String.class));
        } else if (claims.get("type", String.class).equals(TokenType.REFRESH.name())) {
            return new JwtRefreshPayLoadDto(TokenType.REFRESH.name(),
                    Long.parseLong(claims.getSubject()));
        } else {
            throw new InvalidDataException("Token for validation must have type access or refresh");
        }
    }
}
