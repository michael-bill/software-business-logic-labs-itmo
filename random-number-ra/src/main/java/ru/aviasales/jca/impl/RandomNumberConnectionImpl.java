package ru.aviasales.jca.impl;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionMetaData;
import ru.aviasales.jca.RandomNumberConnection;

import java.util.Objects;
import java.util.Random;

public class RandomNumberConnectionImpl implements RandomNumberConnection {

    private final RandomNumberManagedConnection managedConnection;
    private final Random random = new Random();

    public RandomNumberConnectionImpl(RandomNumberManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }

    @Override
    public String generateInvoiceId() throws ResourceException {
        String invoiceId = String.valueOf(random.nextInt(1000000, 10000000)); // Как было у вас
        return invoiceId;
    }

    @Override
    public void close() throws ResourceException {
    }

    @Override
    public jakarta.resource.cci.Interaction createInteraction() throws ResourceException {
        throw new ResourceException("Interaction not supported");
    }

    @Override
    public jakarta.resource.cci.LocalTransaction getLocalTransaction() throws ResourceException {
        throw new ResourceException("LocalTransaction not supported");
    }

    @Override
    public jakarta.resource.cci.ResultSetInfo getResultSetInfo() throws ResourceException {
        throw new ResourceException("ResultSetInfo not supported");
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        throw new ResourceException("MetaData not supported");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RandomNumberConnectionImpl that = (RandomNumberConnectionImpl) o;
        return Objects.equals(managedConnection, that.managedConnection) && Objects.equals(random, that.random);
    }

    @Override
    public int hashCode() {
        return Objects.hash(managedConnection, random);
    }
}
