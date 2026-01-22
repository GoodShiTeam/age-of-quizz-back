package goodshi.ageofquizz.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TrimStringConverter implements AttributeConverter<String, String> {

	@Override
	public String convertToDatabaseColumn(String attribute) {
		return attribute == null ? null : attribute.strip();
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		return dbData;
	}
}
