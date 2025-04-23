package ru.aviasales.jca.impl;

import java.util.Objects;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;
import ru.aviasales.jca.RandomNumberConnection;
import ru.aviasales.jca.RandomNumberConnectionFactory;

import javax.naming.NamingException;
import javax.naming.Reference;

public class RandomNumberConnectionFactoryImpl implements RandomNumberConnectionFactory {

    private final ManagedConnectionFactory mcf;
    private final ConnectionManager cm;
    private Reference reference;

    public RandomNumberConnectionFactoryImpl(ManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }

    @Override
    public RandomNumberConnection getConnection() throws ResourceException {
        if (cm == null) {
            throw new ResourceException("ConnectionManager is not available. Cannot provide a managed connection.");
        }
        return (RandomNumberConnection) cm.allocateConnection(mcf, null);
    }

    @Override
    public Reference getReference() throws NamingException {
        return reference;
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        throw new ResourceException("getMetaData not supported");
    }

    public RecordFactory getRecordFactory() throws ResourceException {
        throw new ResourceException("getRecordFactory not supported");
    }

    public Connection getConnection(ConnectionSpec properties) throws ResourceException {
        throw new ResourceException("getConnection with ConnectionSpec not supported");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RandomNumberConnectionFactoryImpl that = (RandomNumberConnectionFactoryImpl) o;
        return Objects.equals(mcf, that.mcf) && Objects.equals(cm, that.cm) && Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mcf, cm, reference);
    }
}
