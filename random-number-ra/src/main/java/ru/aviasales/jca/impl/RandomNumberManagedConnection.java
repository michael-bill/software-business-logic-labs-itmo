package ru.aviasales.jca.impl;

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;

import javax.transaction.xa.XAResource;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class RandomNumberManagedConnection implements ManagedConnection {

    private final RandomNumberManagedConnectionFactory mcf;
    private PrintWriter logWriter;
    private final List<ConnectionEventListener> listeners = new ArrayList<>();
    private RandomNumberConnectionImpl connectionHandle; // Храним ссылку на выданный хэндл

    public RandomNumberManagedConnection(RandomNumberManagedConnectionFactory mcf) {
        this.mcf = mcf;
    }

    @Override
    public Object getConnection(javax.security.auth.Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        // В простом случае просто создаем новый хэндл
        // В реальном RA здесь могла бы быть логика переиспользования
        if (connectionHandle != null) {
            // Уже выдали, можно бросить исключение или вернуть существующий (зависит от семантики)
            //throw new IllegalStateException("Connection handle already allocated");
        }
        connectionHandle = new RandomNumberConnectionImpl(this);
        return connectionHandle;
    }

    @Override
    public void destroy() throws ResourceException {
        // Очистка ресурсов, если были
        cleanup();
    }

    @Override
    public void cleanup() throws ResourceException {
        // Закрываем все выданные хэндлы (если их может быть несколько)
        if (connectionHandle != null) {
            try {
                connectionHandle.close(); // Уведомляем хэндл, что он закрывается
            } catch (ResourceException ignored) {
            }
            connectionHandle = null; // Сбрасываем ссылку
        }
        // Сбрасываем другие состояния, если есть
    }

    // Уведомляет слушателей (пул соединений), что этот хэндл закрыт
    void closeHandle(RandomNumberConnectionImpl handle) {
        if (handle == this.connectionHandle) {
            ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
            event.setConnectionHandle(handle);
            for (ConnectionEventListener listener : listeners) {
                listener.connectionClosed(event);
            }
            this.connectionHandle = null; // Сбросить ссылку после уведомления
        }
    }


    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof RandomNumberConnectionImpl)) {
            throw new ResourceException("Invalid connection type associated");
        }
        this.connectionHandle = (RandomNumberConnectionImpl) connection;
        // Обычно используется для восстановления связи после диссоциации,
        // например, при возврате соединения в пул.
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        // Наш адаптер не поддерживает XA-транзакции
        throw new NotSupportedException("XA transactions not supported");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        // Наш адаптер не поддерживает локальные транзакции JCA
        throw new NotSupportedException("Local transactions not supported");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        // Можно вернуть базовые метаданные
        return new ManagedConnectionMetaData() {
            @Override
            public String getEISProductName() throws ResourceException {
                return "RandomNumber RA";
            }
            @Override
            public String getEISProductVersion() throws ResourceException {
                return "1.0";
            }
            @Override
            public int getMaxConnections() throws ResourceException {
                return 0; // Неограничено со стороны RA
            }
            @Override
            public String getUserName() throws ResourceException {
                return null; // Аутентификация не используется
            }
        };
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RandomNumberManagedConnection that = (RandomNumberManagedConnection) o;
        return Objects.equals(mcf, that.mcf) && Objects.equals(logWriter, that.logWriter) && Objects.equals(listeners, that.listeners) && Objects.equals(connectionHandle, that.connectionHandle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mcf, logWriter, listeners, connectionHandle);
    }
}
