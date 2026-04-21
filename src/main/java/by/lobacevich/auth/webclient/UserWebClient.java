package by.lobacevich.auth.webclient;

import by.lobacevich.auth.dto.request.UserCreateRequestDto;
import by.lobacevich.auth.dto.response.UserCreatedResponseDto;
import by.lobacevich.auth.exception.ServiceException;
import by.lobacevich.auth.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
@Component
public class UserWebClient {

    private final WebClient userClient;

    @CircuitBreaker(name = "userService")
    public UserCreatedResponseDto create(UserCreateRequestDto createDto) {
        return userClient.post()
                .uri("/users")
                .header("X-User-Id", "0")
                .header("X-Role", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchangeToMono(response -> handleResponse(response, UserCreatedResponseDto.class))
                .onErrorMap(WebClientRequestException.class,
                        ex -> new ServiceUnavailableException("User service is not available"))
                .block();
    }

    @CircuitBreaker(name = "userService")
    public void delete(Long userId) {
        userClient.delete()
                .uri("/users/{id}", userId)
                .header("X-User-Id", "0")
                .header("X-Role", "ROLE_ADMIN")
                .exchangeToMono(response -> handleResponse(response, Void.class))
                .onErrorMap(WebClientRequestException.class,
                        ex -> new ServiceUnavailableException("User service is not available"))
                .block();
    }

    private <T> Mono<T> handleResponse(ClientResponse response, Class<T> clazz) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(clazz);
        }
        return response.bodyToMono(String.class)
                .defaultIfEmpty("User service error")
                .flatMap(body -> Mono.error(new ServiceException(body, response.statusCode())));
    }
}
