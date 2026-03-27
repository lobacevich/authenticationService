package by.lobacevich.auth.controller;

import by.lobacevich.auth.dto.request.LoginRequestDto;
import by.lobacevich.auth.dto.request.RegisterRequestDto;
import by.lobacevich.auth.dto.request.TokenRequestDto;
import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.entity.enums.Role;
import by.lobacevich.auth.security.UserPrincipal;
import by.lobacevich.auth.service.AuthService;
import by.lobacevich.auth.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RefreshTokenControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefreshTokenService tokenService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17.6")
                    .withDatabaseName("test-db")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "VKs3X6p9e8R2tY5w7z9C1f4H6J8kL0nP2qR4sT6uV8wX0yZ2b4c6d8e0f2g4h6j8k0");
        registry.add("jwt.accessTokenExpiration", () -> "900000");
        registry.add("jwt.refreshTokenExpiration", () -> "604800000");
    }

    @Test
    void delete_ShouldReturnNoBodyStatusCode() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(1L),
                        null,
                        List.of(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()))
                );

        authService.register(new RegisterRequestDto(1L, "user1", "123"));
        TokenResponseDto tokenDto = authService.login(new LoginRequestDto("user1", "123"));

        mockMvc.perform(delete("/tokens")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new TokenRequestDto(tokenDto.refreshToken()))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_ShouldReturnUnauthorisedStatusCode() throws Exception {
        mockMvc.perform(delete("/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new TokenRequestDto("TOKEN"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_ShouldReturnForbiddenStatusCode() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(1L),
                        null,
                        List.of(new SimpleGrantedAuthority(Role.ROLE_USER.name()))
                );
        mockMvc.perform(delete("/tokens")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new TokenRequestDto("TOKEN"))))
                .andExpect(status().isForbidden());
    }
}

