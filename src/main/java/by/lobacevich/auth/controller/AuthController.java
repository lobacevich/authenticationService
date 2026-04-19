package by.lobacevich.auth.controller;

import by.lobacevich.auth.dto.request.FullRequestDto;
import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.response.FullDtoResponse;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.orchestrator.RegistrationOrchestrator;
import by.lobacevich.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations such as registration, login,
 * token validation and token refreshing.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationOrchestrator orchestrator;

    /**
     * Registers a new user.
     *
     * @param dto registration data (userId, login, password)
     * @return created user info
     * <p>
     * Possible HTTP responses:
     * <ul>
     *     <li>201 Created – registration successful</li>
     *     <li>400 Bad Request – invalid input or user already exists</li>
     * </ul>
     */
    @PostMapping("/register")
    public ResponseEntity<FullDtoResponse> register(@Valid @RequestBody FullRequestDto dto) {
        return new ResponseEntity<>(orchestrator.register(dto), HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a pair of JWT tokens.
     *
     * @param dto login request containing login and password
     * @return access and refresh tokens
     * <p>
     * Possible HTTP responses:
     * <ul>
     *     <li>200 OK – authentication successful</li>
     *     <li>401 Unauthorized – invalid credentials</li>
     *     <li>404 Not Found – user not found</li>
     * </ul>
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    /**
     * Validates a JWT token and returns its decoded payload.
     *
     * @param tokenDto request containing the token
     * @return decoded token information (e.g. userId, role, token type)
     * <p>
     * Possible HTTP responses:
     * <ul>
     *     <li>200 OK – token is valid</li>
     *     <li>400 Bad Request – token is invalid or expired</li>
     * </ul>
     */
    @PostMapping("/validate")
    public ResponseEntity<Object> validate(@Valid @RequestBody TokenRequestDto tokenDto) {
        return ResponseEntity.ok(authService.validate(tokenDto.token()));
    }

    /**
     * Refreshes the token pair using a refresh token.
     *
     * @param tokenDto request containing the refresh token
     * @return new access and refresh tokens
     * <p>
     * Possible HTTP responses:
     * <ul>
     *     <li>200 OK – successfully refreshed</li>
     *     <li>400 Bad Request – invalid or expired refresh token</li>
     * </ul>
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@Valid @RequestBody TokenRequestDto tokenDto) {
        return ResponseEntity.ok(authService.refresh(tokenDto));
    }
}
