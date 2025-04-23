package ru.aviasales.jca.impl;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;


import javax.security.auth.Subject;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class RandomNumberManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation, Serializable {

    private static final long serialVersionUID = 1L;
    private ResourceAdapter resourceAdapter;
    private PrintWriter logWriter;

    // Конфигурационные свойства (если нужны)
    // @ConfigProperty(defaultValue = "someValue")
    // private String someConfigProperty;

    public RandomNumberManagedConnectionFactory() {
    }

    public RandomNumberManagedConnectionFactory(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        // cxManager = null, если используется вне сервера приложений (не наш случай)
        // или если фабрика не управляется контейнером.
        // В WildFly cxManager будет предоставлен.
        return new RandomNumberConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        // Создаем фабрику без ConnectionManager - она не сможет участвовать в пулинге и транзакциях контейнера.
        return new RandomNumberConnectionFactoryImpl(this, null); // Или бросить исключение, если не поддерживается
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        // Subject и ConnectionRequestInfo игнорируются, т.к. у нас нет аутентификации или спец. запросов
        return new RandomNumberManagedConnection(this);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        // Простая логика: возвращаем любое свободное соединение из пула,
        // так как у нас нет специфичных критериев (subject, cxRequestInfo).
        for (Object obj : connectionSet) {
            if (obj instanceof RandomNumberManagedConnection) {
                return (RandomNumberManagedConnection) obj;
            }
        }
        return null; // Не нашли подходящего
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        if (!(ra instanceof RandomNumberResourceAdapter)) {
            throw new ResourceException("ResourceAdapter must be of type RandomNumberResourceAdapter");
        }
        this.resourceAdapter = ra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RandomNumberManagedConnectionFactory that = (RandomNumberManagedConnectionFactory) o;
        return Objects.equals(resourceAdapter, that.resourceAdapter) && Objects.equals(logWriter, that.logWriter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceAdapter, logWriter);
    }
}
