package ru.aviasales.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.aviasales.processor", "ru.aviasales.common"})
public class AdvertisementProcessorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvertisementProcessorServiceApplication.class, args);
    }

}
