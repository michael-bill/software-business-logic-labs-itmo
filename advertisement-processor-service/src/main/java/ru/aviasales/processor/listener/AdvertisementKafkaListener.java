package ru.aviasales.processor.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.aviasales.common.dto.request.AdvertisementReq;
import ru.aviasales.processor.service.AdvertisementProcessingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdvertisementKafkaListener {

    private final AdvertisementProcessingService processingService;

    @KafkaListener(topics = "${kafka-topics.advertisement-creation-requests}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "advertisementKafkaListenerContainerFactory")
    public void handleAdvertisementCreationRequest(
            @Payload(required = false) AdvertisementReq request,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received message: Key='{}', Topic='{}', Partition={}, Offset={}, Payload='{}'",
                key, topic, partition, offset, request);

        if (request == null) {
            log.error("Received null payload on topic {}. Skipping message. Offset: {}", topic, offset);
            return;
        }

        try {
            processingService.processAdvertisementCreation(request);
            log.info("Successfully processed advertisement request from offset {}", offset);
        } catch (Exception e) {
            log.error("Failed to process advertisement request from offset {}: {}", offset, e.getMessage(), e);
        }
    }
}