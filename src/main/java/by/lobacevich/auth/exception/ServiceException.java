package by.lobacevich.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ServiceException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public ServiceException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
