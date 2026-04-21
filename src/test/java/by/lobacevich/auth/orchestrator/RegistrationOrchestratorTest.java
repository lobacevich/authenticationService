package by.lobacevich.auth.orchestrator;

import by.lobacevich.auth.dto.inner.AuthRegisterDto;
import by.lobacevich.auth.dto.inner.AuthRegisteredDto;
import by.lobacevich.auth.dto.request.FullRequestDto;
import by.lobacevich.auth.dto.request.UserCreateRequestDto;
import by.lobacevich.auth.dto.response.FullDtoResponse;
import by.lobacevich.auth.dto.response.UserCreatedResponseDto;
import by.lobacevich.auth.exception.ServiceException;
import by.lobacevich.auth.exception.ServiceUnavailableException;
import by.lobacevich.auth.mapper.RegisterMapper;
import by.lobacevich.auth.service.AuthService;
import by.lobacevich.auth.webclient.UserWebClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationOrchestratorTest {

    private static final Long ID = 1L;
    private static final String ERROR = "Error";

    @Mock
    private AuthService authService;

    @Mock
    private UserWebClient userClient;

    @Mock
    private RegisterMapper mapper;

    @Mock
    private FullRequestDto fullRequestDto;

    @Mock
    private UserCreateRequestDto createRequestDto;

    @Mock
    private UserCreatedResponseDto createdResponseDto;

    @Mock
    private AuthRegisterDto authRegisterDto;

    @Mock
    private AuthRegisteredDto authRegisteredDto;

    @Mock
    private FullDtoResponse fullResponseDto;

    @InjectMocks
    private RegistrationOrchestrator orchestrator;

    @Test
    void register_ShouldCreateAndRegisterUserAndReturnFullDtoResponse() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto)).thenReturn(createdResponseDto);
        when(createdResponseDto.id()).thenReturn(ID);
        when(mapper.toAuthRegisterDto(fullRequestDto, ID)).thenReturn(authRegisterDto);
        when(authService.register(authRegisterDto)).thenReturn(authRegisteredDto);
        when(mapper.toResponseFull(createdResponseDto, authRegisteredDto)).thenReturn(fullResponseDto);

        FullDtoResponse actual = orchestrator.register(fullRequestDto);

        verify(userClient, times(1)).create(createRequestDto);
        verify(authService, times(1)).register(authRegisterDto);
        verify(userClient, never()).delete(any());
        assertEquals(fullResponseDto, actual);
    }

    @Test
    void register_ShouldCreateAndThenDeleteUserAndThrowServiceException() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto)).thenReturn(createdResponseDto);
        when(createdResponseDto.id()).thenReturn(ID);
        when(mapper.toAuthRegisterDto(fullRequestDto, ID)).thenReturn(authRegisterDto);
        when(authService.register(authRegisterDto)).thenThrow(new ServiceException(ERROR, HttpStatus.BAD_REQUEST));

        assertThrows(ServiceException.class, () -> orchestrator.register(fullRequestDto));
        verify(userClient, times(1)).create(createRequestDto);
        verify(authService, times(1)).register(authRegisterDto);
        verify(userClient, times(1)).delete(ID);
        verify(mapper, never()).toResponseFull(any(), any());
    }

    @Test
    void register_ShouldCreateAndNotDeleteUserAndThrowServiceException() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto)).thenReturn(createdResponseDto);
        when(createdResponseDto.id()).thenReturn(ID);
        when(mapper.toAuthRegisterDto(fullRequestDto, ID)).thenReturn(authRegisterDto);
        when(authService.register(authRegisterDto)).thenThrow(new ServiceException(ERROR, HttpStatus.BAD_REQUEST));
        doThrow(new ServiceUnavailableException(ERROR)).when(userClient).delete(ID);

        assertThrows(ServiceException.class, () -> orchestrator.register(fullRequestDto));
        verify(userClient, times(1)).create(createRequestDto);
        verify(authService, times(1)).register(authRegisterDto);
        verify(userClient, times(1)).delete(ID);
        verify(mapper, never()).toResponseFull(any(), any());
    }

    @Test
    void registerUser_ShouldThrowServiceUnavailableExceptionOnUserCreation() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto)).thenThrow(new ServiceUnavailableException(ERROR));

        assertThrows(ServiceUnavailableException.class, () -> orchestrator.register(fullRequestDto));
        verify(userClient, times(1)).create(createRequestDto);
        verify(authService, never()).register(any());
        verify(userClient, never()).delete(any());
    }
}