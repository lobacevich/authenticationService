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

/**
 * REST controller for managing refresh tokens.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/tokens")
public class RefreshTokenController {

    private final RefreshTokenService service;

    /**
     * Deletes a refresh token.
     * Accessible only to users with ADMIN role.
     *
     * @param tokenDto request containing the refresh token to be deleted
     * @return empty response with status 204 No Content
     * <p>
     * Possible HTTP responses:
     * <ul>
     *     <li>204 No Content – token successfully deleted</li>
     *     <li>400 Bad Request – invalid token</li>
     *     <li>401 Unauthorized – authentication required</li>
     *     <li>403 Forbidden – insufficient permissions</li>
     * </ul>
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody TokenRequestDto tokenDto) {
        service.delete(tokenDto.token());
        return ResponseEntity.noContent().build();
    }
}
