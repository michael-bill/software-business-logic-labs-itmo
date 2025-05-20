package ru.aviasales.admin.configuration;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaClientConfig {

    @Value("${camunda.bpm.client.base-url:http://localhost:8090/engine-rest}")
    private String camundaBaseUrl;

    @Bean
    public ExternalTaskClient externalTaskClient() {
        return ExternalTaskClient.create()
                .baseUrl(camundaBaseUrl)
                .asyncResponseTimeout(20000)
                .build();
    }
}
