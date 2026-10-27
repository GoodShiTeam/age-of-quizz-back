package goodshi.ageofquizz.entity.converter;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Stocke une liste de libellés (sans retour à la ligne) dans une colonne texte, un par ligne. */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		return attribute == null ? "" : String.join("\n", attribute);
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		return dbData == null || dbData.isEmpty() ? new ArrayList<>() : new ArrayList<>(List.of(dbData.split("\n")));
	}
}
