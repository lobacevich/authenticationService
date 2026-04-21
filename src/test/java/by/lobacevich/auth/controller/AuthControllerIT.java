package by.lobacevich.auth.controller;

import by.lobacevich.auth.dto.response.TokenResponseDto;
import by.lobacevich.auth.entity.Credential;
import by.lobacevich.auth.repository.CredentialRepository;
import by.lobacevich.auth.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthControllerIT {

    private static final String FULL_REQUEST_DTO = """
            {
                "login": "login",
                "password": "password",
                "name": "John",
                "surname": "Doe",
                "birthDate": "1990-01-01",
                "email": "john@example.com"
            }
            """;
    private static final String USER_RESPONSE = """
            {
                "id": 1,
                "name": "John",
                "surname": "Doe",
                "birthDate": "1990-01-01",
                "email": "john@example.com",
                "active": true,
                "createdAt": "2026-01-01T10:00:00",
                "updatedAt": "2026-01-01T10:00:00"
            }
            """;
    private static final String DUPLICATE_EMAIL = """
            {
                "message": "Duplicate email"
            }
            """;

    @Autowired
    private JwtTokenService service;

    @Autowired
    private CredentialRepository repository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder encoder;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17.6")
                    .withDatabaseName("test-db")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "VKs3X6p9e8R2tY5w7z9C1f4H6J8kL0nP2qR4sT6uV8wX0yZ2b4c6d8e0f2g4h6j8k0");
        registry.add("jwt.accessTokenExpiration", () -> "900000");
        registry.add("jwt.refreshTokenExpiration", () -> "604800000");
        registry.add("userClient.url", () -> wireMock.baseUrl());
    }

    @BeforeEach
    void cleanDbAndWireMock() {
        repository.deleteAll();
        wireMock.resetAll();
    }

    @Test
    void register_ShouldReturnUserDtoResponse() throws Exception {
        wireMock.stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(USER_RESPONSE)));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FULL_REQUEST_DTO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("login"))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void register_ShouldReturnErrorWhenUserServiceFails() throws Exception {
        wireMock.stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(DUPLICATE_EMAIL)));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FULL_REQUEST_DTO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate email"));
    }

    @Test
    void register_ShouldReturnErrorWhenAuthServiceFailsAndRollBackUserService() throws Exception {
        repository.save(
                Credential.builder()
                        .userId(1L)
                        .login("login")
                        .passwordHash(encoder.encode("password"))
                        .build()
        );

        wireMock.stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(USER_RESPONSE)));

        wireMock.stubFor(WireMock.delete(urlPathEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(204)));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FULL_REQUEST_DTO))
                .andExpect(status().isBadRequest());

        wireMock.verify(exactly(1), deleteRequestedFor(urlPathEqualTo("/users/1")));
    }

    @Test
    void register_ShouldReturnErrorWhenAuthServiceFailsAndFailDeleteUser() throws Exception {
        repository.save(
                Credential.builder()
                        .userId(1L)
                        .login("login")
                        .passwordHash(encoder.encode("password"))
                        .build()
        );

        wireMock.stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(USER_RESPONSE)));

        wireMock.stubFor(WireMock.delete(urlPathEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(503)));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FULL_REQUEST_DTO))
                .andExpect(status().isBadRequest());

        wireMock.verify(exactly(1), deleteRequestedFor(urlPathEqualTo("/users/1")));
    }

    @Test
    void login_ShouldReturnTokenResponseDto() throws Exception {
        String loginJson = """
                {
                  "login": "user1",
                  "password": "password"
                }
                """;

        repository.save(Credential.builder()
                .userId(1L)
                .login("user1")
                .passwordHash(encoder.encode("password"))
                .build());

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        TokenResponseDto response = objectMapper.readValue(
                responseBody,
                TokenResponseDto.class
        );
        Claims access = service.parse(response.accessToken());
        Claims refresh = service.parse(response.refreshToken());

        assertEquals(1L, Long.parseLong(access.getSubject()));
        assertEquals("ROLE_USER", access.get("role"));
        assertEquals(1L, Long.parseLong(refresh.getSubject()));
    }

    @Test
    void validate_ShouldReturnIsOkStatusCode() throws Exception {
        repository.save(
                Credential.builder()
                        .userId(1L)
                        .login("user1")
                        .passwordHash(encoder.encode("password"))
                        .build()
        );

        String loginJson = """
                {
                  "login": "user1",
                  "password": "password"
                }
                """;
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponseDto responseDto = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                TokenResponseDto.class
        );

        String validateJson = """
                {
                  "token": "%s"
                }
                """.formatted(responseDto.accessToken());

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validateJson))
                .andExpect(status().isOk());
    }

    @Test
    void validate_ShouldReturnBadRequestStatus() throws Exception {
        String json = """
                {
                  "token": "invalid_token"
                }
                """;

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_ShouldReturnTokenResponseDto() throws Exception {
        repository.save(
                Credential.builder()
                        .userId(1L)
                        .login("user1")
                        .passwordHash(encoder.encode("password"))
                        .build()
        );

        String loginJson = """
                {
                  "login": "user1",
                  "password": "password"
                }
                """;
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        TokenResponseDto responseDto = objectMapper.readValue(
                responseBody,
                TokenResponseDto.class
        );
        String refreshJson = """
                {
                  "token": "%s"
                }
                """.formatted(responseDto.refreshToken());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk());
    }
}
