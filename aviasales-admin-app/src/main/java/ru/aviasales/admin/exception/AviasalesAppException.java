package ru.aviasales.admin.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AviasalesAppException extends RuntimeException {

    private final HttpStatus status;

    public AviasalesAppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
