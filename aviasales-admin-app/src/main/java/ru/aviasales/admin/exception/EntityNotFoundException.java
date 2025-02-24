package ru.aviasales.admin.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends AviasalesAppException {
    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
