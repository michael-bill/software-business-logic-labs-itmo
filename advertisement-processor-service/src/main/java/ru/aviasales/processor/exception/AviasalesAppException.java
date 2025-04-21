package ru.aviasales.processor.exception;

import lombok.Getter;
@Getter
public abstract class AviasalesAppException extends RuntimeException {
    public AviasalesAppException(String message) {
        super(message);
    }
}
