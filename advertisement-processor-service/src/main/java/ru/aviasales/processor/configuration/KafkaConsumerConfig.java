package ru.aviasales.processor.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.aviasales.common.dto.request.AdvertisementReq;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private final ObjectMapper objectMapper;

    @Bean
    public ConsumerFactory<String, AdvertisementReq> advertisementConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.aviasales.common.dto.request.AdvertisementReq");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "ru.aviasales.common.dto.request");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");

        return new DefaultKafkaConsumerFactory<>(props);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AdvertisementReq> advertisementKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AdvertisementReq> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(advertisementConsumerFactory());
        return factory;
    }
}