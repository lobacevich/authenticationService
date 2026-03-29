package by.lobacevich.auth.service;

import by.lobacevich.auth.entity.enums.Role;
import by.lobacevich.auth.entity.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    public static final Long ID = 1L;
    public static final String INVALID_TOKEN = "invalidToken";
    public static final String SECRET = "VKs3X6p9e8R2tY5w7z9C1f4H6J8kL0nP2qR4sT6uV8wX0yZ2b4c6d8e0f2g4h6j8k0";
    public static final long ACCESS_EXPIRATION = 900000;
    public static final long REFRESH_EXPIRATION = 604800000;

    private final JwtTokenService service = new JwtTokenService(SECRET,
            ACCESS_EXPIRATION,
            REFRESH_EXPIRATION);

    @Test
    void generateAccessToken_ShouldGenerateTokenWithUserIdAndRole() {
        String accessToken = service.generateAccessToken(ID, Role.ROLE_USER);

        Claims claims = service.parse(accessToken);

        assertEquals(ID, Long.parseLong(claims.getSubject()));
        assertEquals(Role.ROLE_USER.name(), claims.get("role"));
        assertEquals(TokenType.ACCESS.name(), claims.get("type"));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateRefreshToken_ShouldGenerateRefreshTokenWithUserId() {
        String refreshToken = service.generateRefreshToken(ID);

        Claims claims = service.parse(refreshToken);

        assertEquals(ID, Long.parseLong(claims.getSubject()));
        assertEquals(TokenType.REFRESH.name(), claims.get("type"));
        assertNull(claims.get("role"));
    }

    @Test
    void parse_ShouldThrowException() {
        assertThrows(JwtException.class, () -> service.parse(INVALID_TOKEN));
        assertThrows(IllegalArgumentException.class, () -> service.parse(null));
    }
}
