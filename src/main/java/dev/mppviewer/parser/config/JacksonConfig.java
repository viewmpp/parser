package dev.mppviewer.parser.config;

import dev.mppviewer.parser.model.Contract;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer contractJsonCustomizer() {
        SimpleModule dates = new SimpleModule().addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Contract.DATE_TIME_PATTERN)));

        return builder -> builder
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .addModule(dates);
    }
}
