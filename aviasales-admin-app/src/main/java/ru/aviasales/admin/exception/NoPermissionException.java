package ru.aviasales.admin.exception;

import org.springframework.http.HttpStatus;

public class NoPermissionException extends AviasalesAppException {
    public NoPermissionException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
