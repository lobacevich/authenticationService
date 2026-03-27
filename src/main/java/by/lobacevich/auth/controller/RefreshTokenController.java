package by.lobacevich.auth.controller;

import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tokens")
public class RefreshTokenController {

    private final RefreshTokenService service;

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody TokenRequestDto tokenDto) {
        service.delete(tokenDto.token());
        return ResponseEntity.noContent().build();
    }
}
