package ru.aviasales.admin.exception;

import org.springframework.http.HttpStatus;

public class OptimisticLockException extends AviasalesAppException {
    public OptimisticLockException() {
        super("Изменения не были применены, так как объект был изменен " +
                "другим пользователем. Обновите данные и повторите попытку.", HttpStatus.CONFLICT);
    }
}
