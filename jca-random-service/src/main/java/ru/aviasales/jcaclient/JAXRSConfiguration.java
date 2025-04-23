package ru.aviasales.jcaclient;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api") // Базовый путь для всех REST эндпоинтов
public class JAXRSConfiguration extends Application {
    // Конфигурация JAX-RS через аннотации
}
