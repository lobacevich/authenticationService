package by.lobacevich.auth.service.impl;

import by.lobacevich.auth.dto.inner.AuthRegisterDto;
import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.response.JwtAccessPayLoadDto;
import by.lobacevich.auth.dto.response.JwtRefreshPayLoadDto;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.entity.Credential;
import by.lobacevich.auth.entity.enums.Role;
import by.lobacevich.auth.entity.enums.TokenType;
import by.lobacevich.auth.exception.EntityNotFoundException;
import by.lobacevich.auth.exception.IncorrectPasswordException;
import by.lobacevich.auth.exception.InvalidDataException;
import by.lobacevich.auth.repository.CredentialRepository;
import by.lobacevich.auth.service.JwtTokenService;
import by.lobacevich.auth.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    public static final Long ID = 1L;
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_HASH = "encoded";
    public static final String TOKEN = "access";
    public static final String REFRESH_TOKEN = "refresh";
    public static final String NEW_REFRESH_TOKEN = "newRefresh";
    public static final AuthRegisterDto REGISTER_DTO = new AuthRegisterDto(LOGIN, PASSWORD, ID);
    public static final String INCORRECT_TOKEN_TYPE = "INCORRECT";

    @Mock
    private CredentialRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtTokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Credential user;

    @Mock
    private LoginRequestDto loginDto;

    @Mock
    private Claims claims;

    @Mock
    private TokenRequestDto tokenRequestDto;

    @InjectMocks
    private AuthServiceImpl service;

    @Captor
    private ArgumentCaptor<Credential> userCaptor;

    @Test
    void register_ShouldSaveUser() {
        when(repository.existsById(ID)).thenReturn(false);
        when(encoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);

        service.register(REGISTER_DTO);

        verify(repository, times(1)).save(userCaptor.capture());

        Credential saved = userCaptor.getValue();

        assertEquals(LOGIN, saved.getLogin());
        assertEquals(ID, saved.getUserId());
        assertEquals(Role.ROLE_USER, saved.getRole());
    }

    @Test
    void register_ShouldThrowInvalidDataExceptionById() {
        when(repository.existsById(ID)).thenReturn(true);

        assertThrows(InvalidDataException.class, () -> service.register(REGISTER_DTO));
    }

    @Test
    void login_ShouldReturnTokenResponseDto() {
        when(loginDto.login()).thenReturn(LOGIN);
        when(repository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(loginDto.password()).thenReturn(PASSWORD);
        when(user.getPasswordHash()).thenReturn(PASSWORD_HASH);
        when(encoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(user.getUserId()).thenReturn(ID);
        when(user.getRole()).thenReturn(Role.ROLE_USER);
        when(tokenService.generateAccessToken(ID, Role.ROLE_USER)).thenReturn(TOKEN);
        when(tokenService.generateRefreshToken(ID)).thenReturn(REFRESH_TOKEN);
        doNothing().when(refreshTokenService).save(REFRESH_TOKEN, user);

        TokenResponseDto actual = service.login(loginDto);

        verify(refreshTokenService, times(1)).save(REFRESH_TOKEN, user);
        assertEquals(TOKEN, actual.accessToken());
        assertEquals(REFRESH_TOKEN, actual.refreshToken());
    }

    @Test
    void login_ShouldThrowEntityNotFoundException() {
        when(loginDto.login()).thenReturn(LOGIN);
        when(repository.findByLogin(LOGIN)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.login(loginDto));
    }

    @Test
    void login_ShouldThrowIncorrectPasswordException() {
        when(loginDto.login()).thenReturn(LOGIN);
        when(repository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(loginDto.password()).thenReturn(PASSWORD);
        when(user.getPasswordHash()).thenReturn(PASSWORD_HASH);
        when(encoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThrows(IncorrectPasswordException.class, () -> service.login(loginDto));
    }

    @Test
    void refresh_ShouldReturnTokenResponseDto() {
        when(tokenRequestDto.token()).thenReturn(REFRESH_TOKEN);
        when(tokenService.parse(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(ID.toString());
        when(repository.findById(ID)).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(Role.ROLE_USER);
        when(tokenService.generateAccessToken(ID, Role.ROLE_USER)).thenReturn(TOKEN);
        when(tokenService.generateRefreshToken(ID)).thenReturn(NEW_REFRESH_TOKEN);
        doNothing().when(refreshTokenService).refresh(REFRESH_TOKEN, NEW_REFRESH_TOKEN);

        TokenResponseDto actual = service.refresh(tokenRequestDto);

        verify(refreshTokenService, times(1)).refresh(REFRESH_TOKEN, NEW_REFRESH_TOKEN);
        assertEquals(TOKEN, actual.accessToken());
        assertEquals(NEW_REFRESH_TOKEN, actual.refreshToken());
    }

    @Test
    void refresh_ShouldThrowEntityNotFoundException() {
        when(tokenRequestDto.token()).thenReturn(REFRESH_TOKEN);
        when(tokenService.parse(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(ID.toString());
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.refresh(tokenRequestDto));
    }

    @Test
    void validate_ShouldReturnJwtAccessPayLoadDto() {
        when(tokenService.parse(TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn(TokenType.ACCESS.name());
        when(claims.getSubject()).thenReturn(ID.toString());
        when(claims.get("role", String.class)).thenReturn(Role.ROLE_USER.name());

        JwtAccessPayLoadDto actual = (JwtAccessPayLoadDto) service.validate(TOKEN);

        assertEquals(TokenType.ACCESS.name(), actual.type());
        assertEquals(Role.ROLE_USER.name(), actual.role());
        assertEquals(ID, actual.userId());
    }

    @Test
    void validate_ShouldReturnJwtRefreshPayLoadDto() {
        when(tokenService.parse(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn(TokenType.REFRESH.name());
        when(claims.getSubject()).thenReturn(ID.toString());

        JwtRefreshPayLoadDto actual = (JwtRefreshPayLoadDto) service.validate(REFRESH_TOKEN);

        assertEquals(TokenType.REFRESH.name(), actual.type());
        assertEquals(ID, actual.userId());
    }

    @Test
    void validate_ShouldThrowInvalidDataException() {
        when(tokenService.parse(TOKEN)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn(INCORRECT_TOKEN_TYPE);

        assertThrows(InvalidDataException.class, () -> service.validate(TOKEN));
    }
}