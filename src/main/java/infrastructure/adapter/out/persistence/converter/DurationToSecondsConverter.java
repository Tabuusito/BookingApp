package infrastructure.adapter.out.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = true) // autoApply = true para que se aplique a todos los campos Duration
public class DurationToSecondsConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        return (attribute == null ? null : attribute.getSeconds());
    }

    @Override
    public Duration convertToEntityAttribute(Long dbData) {
        return (dbData == null ? null : Duration.ofSeconds(dbData));
    }
}
