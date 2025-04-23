package ru.aviasales.jca;

import jakarta.resource.cci.Connection;

public interface RandomNumberConnection extends Connection {
    String generateInvoiceId() throws jakarta.resource.ResourceException;
}
