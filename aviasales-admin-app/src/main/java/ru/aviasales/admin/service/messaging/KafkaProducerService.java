package ru.aviasales.admin.service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.aviasales.common.dto.request.AdvertisementReq;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, AdvertisementReq> advertisementKafkaTemplate;

    @Value("${kafka-topics.advertisement-creation-requests}")
    private String advertisementCreationTopic;

    public void sendAdvertisementRequest(AdvertisementReq request) {
        log.info("Sending advertisement creation request to Kafka topic '{}': {}", advertisementCreationTopic, request);
        try {
            CompletableFuture<SendResult<String, AdvertisementReq>> future =
                    advertisementKafkaTemplate.send(advertisementCreationTopic, request);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent message=[{}] with offset=[{}] to topic=[{}]",
                            request, result.getRecordMetadata().offset(), advertisementCreationTopic);
                } else {
                    log.error("Unable to send message=[{}] due to : {}", request, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending advertisement request to Kafka: {}", e.getMessage(), e);
        }
    }
}
