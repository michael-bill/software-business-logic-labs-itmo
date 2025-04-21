package ru.aviasales.processor.exception;

public class EntityNotFoundException extends AviasalesAppException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
