package goodshi.ageofquizz.service;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ImportDataService {
	public byte[] exportToBytes(String jsonContent) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(jsonContent);

		String civId = root.get("civ_id").asText();

		Map<String, String> buildingMap = new HashMap<>();
		for (JsonNode b : root.get("civ_techs_buildings")) {
			String type = safeGetText(b, "Draw Node Type");
			if (type.equals("Building")) {
				String id = safeGetText(b, "Building ID");
				String name = safeGetText(b, "Name");
				if (!id.isEmpty() && !name.isEmpty()) {
					buildingMap.put(id, name);
				}
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

		// Header
		writer.write("Civilization,Name,Category,Age,NodeType,Available,Building\n");

		// Buildings
		processSection(writer, root.get("civ_techs_buildings"), civId, "Building", buildingMap);

		// Units & Techs
		processSection(writer, root.get("civ_techs_units"), civId, "Unit/Tech", buildingMap);

		writer.flush();
		writer.close();

		return out.toByteArray();
	}

	private static void processSection(BufferedWriter writer, JsonNode section, String civId, String category,
			Map<String, String> buildingMap) throws IOException {

		for (JsonNode node : section) {

			String name = safeGetText(node, "Name");
			String age = safeGetText(node, "Age ID");
			String nodeType = safeGetText(node, "Node Type");
			String status = safeGetText(node, "Node Status");
			boolean available = !status.equalsIgnoreCase("NotAvailable");

			// Building ID → nom
			String buildingName = "";
			if (node.has("Building ID") && !node.get("Building ID").isNull()) {
				buildingName = buildingMap.getOrDefault(node.get("Building ID").asText(), "Unknown");
			}

			writer.write(civId + "," + escape(name) + "," + category + "," + age + "," + nodeType + ","
					+ (available ? "YES" : "NO") + "," + escape(buildingName) + "\n");
		}
	}

	private static String escape(String value) {
		if (value.contains(",")) {
			return "\"" + value + "\"";
		}
		return value;
	}

	private static String safeGetText(JsonNode node, String fieldName) {
		if (node.has(fieldName) && !node.get(fieldName).isNull()) {
			return node.get(fieldName).asText();
		}
		return "";
	}

}
