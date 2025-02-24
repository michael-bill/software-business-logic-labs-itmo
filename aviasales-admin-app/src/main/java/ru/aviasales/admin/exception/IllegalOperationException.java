package ru.aviasales.admin.exception;

import org.springframework.http.HttpStatus;

public class IllegalOperationException extends AviasalesAppException {
    public IllegalOperationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
