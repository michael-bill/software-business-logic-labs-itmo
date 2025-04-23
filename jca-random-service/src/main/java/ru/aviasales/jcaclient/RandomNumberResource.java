package ru.aviasales.jcaclient;

// import jakarta.annotation.Resource; // Больше не используется
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Импортируем классы реализации напрямую
import ru.aviasales.jca.impl.RandomNumberConnectionFactoryImpl;
import ru.aviasales.jca.impl.RandomNumberManagedConnectionFactory;
import ru.aviasales.jca.RandomNumberConnection;
import jakarta.resource.ResourceException; // Оставляем на всякий случай

@Path("/random")
@RequestScoped
public class RandomNumberResource {

    // @Resource(lookup = "java:/eis/RandomNumberCF") // УДАЛЕНО
    // private RandomNumberConnectionFactory connectionFactory; // УДАЛЕНО

    // !!! ВМЕСТО ИНЪЕКЦИИ - РУЧНОЕ СОЗДАНИЕ (УПРОЩЕННЫЙ ВАРИАНТ) !!!
    // Это НЕ использует пул и управление WildFly. Просто пример.
    private RandomNumberConnection getConnectionDirectly() throws ResourceException {
        // Самый простой, но неправильный с точки зрения JCA способ:
        // Создаем напрямую реализацию Connection. ManagedConnection игнорируется.
        // В реальном сценарии без RA это было бы не так.
        // Здесь мы сохраняем JCA классы для примера, но это обходной путь.
        return new ru.aviasales.jca.impl.RandomNumberConnectionImpl(null); // Передаем null вместо ManagedConnection
    }
    // ====================================================================

    @GET
    @Path("/invoice-id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRandomInvoiceId() {
        RandomNumberConnection connection = null;
        try {
            // Получаем соединение напрямую (минуя JCA lookup и пул)
            connection = getConnectionDirectly();
            if (connection == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Failed to create connection instance directly\"}")
                        .build();
            }

            String invoiceId = connection.generateInvoiceId();

            // Возвращаем JSON объект
            return Response.ok("{\"invoiceId\": \"" + invoiceId + "\"}").build();

        } catch (ResourceException e) {
            // Ловим ResourceException, т.к. метод getConnectionDirectly его пробрасывает
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
                    connection.close(); // Вызовет closeHandle(this) внутри, но MC=null
                } catch (ResourceException e) {
                    // Логируем ошибку закрытия, если нужно
                }
            }
        }
    }
}
