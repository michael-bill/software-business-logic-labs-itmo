package ru.aviasales.admin.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.aviasales.admin.dto.response.ErrorResp;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResp> handleException(Exception ex) {
        log.error("Пятисотка: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResp.builder()
                        .message("При выполнении запроса произошла непредвиденная ошибка, " +
                                "обратитесь в техническую поддержку")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(AviasalesAppException.class)
    protected ResponseEntity<ErrorResp> handleAviasalesAppException(AviasalesAppException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResp.builder()
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResp> handleValidationException(ConstraintViolationException ex) {
        return new ResponseEntity<>(
                ErrorResp.builder()
                        .message(ex.getConstraintViolations().stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", ")))
                        .timestamp(LocalDateTime.now())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResp> handleValidationException(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(
                ErrorResp.builder()
                        .message(ex.getBindingResult().getAllErrors().stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .collect(Collectors.joining(", ")))
                        .timestamp(LocalDateTime.now())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<ErrorResp> handleValidationException(HandlerMethodValidationException ex) {
        return new ResponseEntity<>(
                ErrorResp.builder()
                        .message("Указаны некорректные параметры запроса")
                        .timestamp(LocalDateTime.now())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    protected ResponseEntity<ErrorResp> handleValidationException(HttpMessageConversionException ex) {
        return new ResponseEntity<>(
                ErrorResp.builder()
                        .message("Указаны некорректные параметры запроса")
                        .timestamp(LocalDateTime.now())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResp> handleValidationException(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(
                ErrorResp.builder()
                        .message("Указаны некорректные параметры запроса")
                        .timestamp(LocalDateTime.now())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }
}
