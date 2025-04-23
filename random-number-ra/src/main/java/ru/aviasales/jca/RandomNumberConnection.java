package ru.aviasales.jca;

import jakarta.resource.cci.Connection;

public interface RandomNumberConnection extends Connection {
    /**
     * Генерирует и возвращает строковое представление случайного ID.
     * @return Случайный ID в виде строки.
     */
    String generateInvoiceId() throws jakarta.resource.ResourceException;
}
