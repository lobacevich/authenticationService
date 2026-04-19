package by.lobacevich.auth.orchestrator;

import by.lobacevich.auth.dto.inner.AuthRegisteredDto;
import by.lobacevich.auth.dto.request.FullRequestDto;
import by.lobacevich.auth.dto.response.FullDtoResponse;
import by.lobacevich.auth.dto.response.UserCreatedResponseDto;
import by.lobacevich.auth.mapper.RegisterMapper;
import by.lobacevich.auth.service.AuthService;
import by.lobacevich.auth.webclient.UserWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
@Service
public class RegistrationOrchestrator {

    private final UserWebClient userWebClient;
    private final AuthService authService;
    private final RegisterMapper mapper;

    public FullDtoResponse register(FullRequestDto requestDto) {

        UserCreatedResponseDto userDto = userWebClient.create(mapper.toUserCreateRequest(requestDto));
        Long userId = userDto.id();
        log.info("User created in userService with id: {}", userId);

        try {
            AuthRegisteredDto registeredDto = authService.register(mapper.toAuthRegisterDto(requestDto, userId));
            log.info("User registered in authService with id: {}", userId);
            return mapper.toResponseFull(userDto, registeredDto);
        } catch (Exception e) {
            log.info("Registration failed, rollback user {}", userId);
            try {
                userWebClient.delete(userId);
                log.info("Rollback success for user {}", userId);
            } catch (Exception exception) {
                log.error("Rollback failed for user {}, {}", userId, e.getStackTrace());
            }
            throw e;
        }
    }
}
