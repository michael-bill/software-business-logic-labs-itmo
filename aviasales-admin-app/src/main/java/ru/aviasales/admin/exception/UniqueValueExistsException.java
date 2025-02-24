package ru.aviasales.admin.exception;

import org.springframework.http.HttpStatus;

public class UniqueValueExistsException extends AviasalesAppException {
    public UniqueValueExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
