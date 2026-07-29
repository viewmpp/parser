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

    /**
     * Стандартный ISO опускает нулевые секунды и выдаёт 2026-01-05T08:00, а контракт
     * требует полную форму всегда: JS читает 2026-01-05T08:00:00 как местное время.
     * Формат задан глобально, чтобы его нельзя было забыть на новом поле.
     *
     * NON_NULL не включаем намеренно: parent_id: null означает корень дерева, и
     * пропавшее поле нельзя будет отличить от забытого.
     */
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
