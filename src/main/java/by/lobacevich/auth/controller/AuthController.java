package by.lobacevich.auth.controller;

import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.RegisterRequestDto;
import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.dto.response.UserDtoResponse;
import by.lobacevich.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDtoResponse> register(@Valid @RequestBody RegisterRequestDto dto) {
        return new ResponseEntity<>(authService.register(dto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@Valid @RequestBody TokenRequestDto tokenDto) {
        authService.validate(tokenDto.token());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@Valid @RequestBody TokenRequestDto tokenDto) {
        return ResponseEntity.ok(authService.refresh(tokenDto));
    }
}
