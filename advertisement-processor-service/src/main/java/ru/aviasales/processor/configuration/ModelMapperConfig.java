package ru.aviasales.processor.configuration;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true)
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setPropertyCondition(Conditions.isNotNull());
        return modelMapper;
    }
}
