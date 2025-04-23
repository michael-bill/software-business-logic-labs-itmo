package ru.aviasales.jca.impl;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import java.io.Serializable;
import java.util.Objects;

import javax.transaction.xa.XAResource;

@Connector(
        description = "Random Number Resource Adapter",
        displayName = "RandomNumberRA",
        vendorName = "Aviasales"
)
public class RandomNumberResourceAdapter implements ResourceAdapter, Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RandomNumberResourceAdapter that = (RandomNumberResourceAdapter) o;
        return Objects.equals(bootstrapContext, that.bootstrapContext);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bootstrapContext);
    }

    public RandomNumberResourceAdapter(BootstrapContext bootstrapContext) {
        this.bootstrapContext = bootstrapContext;
    }

    private static final long serialVersionUID = 1L;

    private transient BootstrapContext bootstrapContext;

    public RandomNumberResourceAdapter() {
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        this.bootstrapContext = ctx;
    }

    @Override
    public void stop() {
        this.bootstrapContext = null;
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        throw new UnsupportedOperationException("Endpoint activation is not supported by this resource adapter.");
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return null;
    }
}
