package ru.aviasales.jcaclient;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.aviasales.jca.RandomNumberConnection;
import ru.aviasales.jca.RandomNumberConnectionFactory;

@Path("/random")
@RequestScoped // Управляется CDI
public class RandomNumberResource {

    // === ИЗМЕНЕНИЕ ЗДЕСЬ ===
    // Пробуем имя, часто используемое WildFly/JBoss по умолчанию для CF
    @Resource(lookup = "java:/eis/RandomNumberCF")
    private RandomNumberConnectionFactory connectionFactory;
    // ======================

    @GET
    @Path("/invoice-id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRandomInvoiceId() {
        if (connectionFactory == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Resource Adapter Connection Factory not available (lookup failed)\"}") // Уточнили ошибку
                    .build();
        }

        RandomNumberConnection connection = null; // Объявляем заранее
        try {
            connection = connectionFactory.getConnection();

            String invoiceId = connection.generateInvoiceId();

            // Возвращаем JSON объект
            return Response.ok("{\"invoiceId\": \"" + invoiceId + "\"}").build();

        } catch (jakarta.resource.ResourceException e) { // Уточнили тип исключения

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to get or use JCA connection: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Unexpected error: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            if (connection != null) {
                try {

                    connection.close();

                } catch (jakarta.resource.ResourceException e) {

                    // Ошибка закрытия обычно не фатальна для запроса, но логируем
                }
            } else {

            }
        }
    }
}
