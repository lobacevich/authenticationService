package by.lobacevich.auth.exceptionhandler;

import by.lobacevich.auth.dto.response.ErrorDto;
import by.lobacevich.auth.exception.EntityNotFoundException;
import by.lobacevich.auth.exception.IncorrectPasswordException;
import by.lobacevich.auth.exception.InvalidDataException;
import by.lobacevich.auth.exception.ServiceException;
import by.lobacevich.auth.exception.ServiceUnavailableException;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Log4j2
@RestControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_LOG_FRAME = "{}, {}";

    @ExceptionHandler({InvalidDataException.class,
            JwtException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorDto> handleInvalidDataException(Exception e) {
        log.error(ERROR_LOG_FRAME, e.getMessage(), e.getClass().getSimpleName());
        return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(Exception e) {
        log.error(ERROR_LOG_FRAME, e.getMessage(), e.getClass().getSimpleName());
        return new ResponseEntity<>(new ErrorDto("Login already exists"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEntityNotFoundException(EntityNotFoundException e) {
        return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ErrorDto> handleIncorrectPasswordException(EntityNotFoundException e) {
        return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return new ResponseEntity<>(new ErrorDto(String.join(", ", errors)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorDto> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<String> handleServiceException(ServiceException e) {
        log.error(ERROR_LOG_FRAME, e.getMessage(), e.getStackTrace());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorDto> handleServiceUnavailableException(ServiceUnavailableException e) {
        log.error(ERROR_LOG_FRAME, e.getMessage(), e.getStackTrace());
        return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception e) {
        log.error("{}, {}, {}", "Unhandled exception", e.getMessage(), e.getClass().getSimpleName());
        return new ResponseEntity<>(new ErrorDto("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
