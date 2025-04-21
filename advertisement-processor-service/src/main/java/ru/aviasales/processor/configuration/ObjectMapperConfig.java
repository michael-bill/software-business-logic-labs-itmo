package ru.aviasales.processor.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
public class ObjectMapperConfig {
    @Bean
    @Primary
    @SuppressWarnings("all")
    public ObjectMapper objectMapper() {
        SimpleModule enumModule = new SimpleModule();
        enumModule.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<Enum> modifyEnumDeserializer(
                    DeserializationConfig config,
                    JavaType type,
                    BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer
            ) {
                return new StdDeserializer<>(type) {
                    @Override
                    public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        String value = p.getValueAsString();
                        if (value == null) {
                            return null;
                        }
                        return Enum.valueOf((Class<Enum>) type.getRawClass(), value.toUpperCase());
                    }
                };
            }
        });

        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(enumModule)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

}
