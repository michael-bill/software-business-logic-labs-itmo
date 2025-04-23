package ru.aviasales.jcaclient;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ru.aviasales.jca.RandomNumberConnection;
import jakarta.resource.ResourceException;
import ru.aviasales.jca.impl.RandomNumberConnectionImpl;

@Path("/random")
@RequestScoped
public class RandomNumberResource {

    @GET
    @Path("/invoice-id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRandomInvoiceId() {
        RandomNumberConnection connection = null;
        try {
            connection = getConnectionDirectly();
            if (connection == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Failed to create connection instance directly\"}")
                        .build();
            }

            String invoiceId = connection.generateInvoiceId();

            return Response.ok("{\"invoiceId\": \"" + invoiceId + "\"}").build();

        } catch (ResourceException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"ResourceException during direct connection usage: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            // Общая ошибка
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Unexpected error: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            // Пытаемся закрыть напрямую созданное соединение
            if (connection != null) {
                try {
                    connection.close();
                } catch (ResourceException ignored) {}
            }
        }
    }

    private RandomNumberConnection getConnectionDirectly() throws ResourceException {
        return new RandomNumberConnectionImpl(null);
    }
}
