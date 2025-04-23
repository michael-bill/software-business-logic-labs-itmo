package ru.aviasales.admin.jca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JcaRandomServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${jca.random.service.url}/random/invoice-id")
    private String invoiceIdEndpointUrl;

    public String getNewInvoiceId() {
        log.info("Requesting new invoice ID from JCA service: {}", invoiceIdEndpointUrl);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(invoiceIdEndpointUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();
                log.debug("Received response body: {}", responseBody);
                try {
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    JsonNode invoiceIdNode = rootNode.path("invoiceId");
                    if (!invoiceIdNode.isMissingNode() && invoiceIdNode.isTextual()) {
                        String invoiceId = invoiceIdNode.asText();
                        log.info("Successfully retrieved invoice ID from JCA service: {}", invoiceId);
                        return invoiceId;
                    } else {
                        log.error("Invalid JSON response format from JCA service. 'invoiceId' field missing or not text: {}", responseBody);
                        throw new RuntimeException("Invalid response format from JCA random service.");
                    }
                } catch (IOException e) {
                    log.error("Failed to parse JSON response from JCA service: {}", responseBody, e);
                    throw new RuntimeException("Failed to parse response from JCA random service.", e);
                }
            } else {
                log.error("Failed to get invoice ID from JCA service. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to retrieve invoice ID from JCA random service. Status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error calling JCA random service at {}: {}", invoiceIdEndpointUrl, e.getMessage());
            throw new RuntimeException("Could not connect to JCA random service: " + e.getMessage(), e);
        }
    }
}
