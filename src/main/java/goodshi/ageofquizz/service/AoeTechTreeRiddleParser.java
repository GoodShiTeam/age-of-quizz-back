package goodshi.ageofquizz.service;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import goodshi.ageofquizz.model.DailyRiddleData;
import goodshi.ageofquizz.model.DailyRiddleData.Entry;
import goodshi.ageofquizz.model.DailyRiddleData.Hint;

/**
 * Transforme les fichiers du projet SiegeEngineers/aoe2techtree
 * (data/data.json et data/locales/fr/strings.json) en énigmes : une entrée
 * par civilisation et par unité (hors versions élite et héros), avec 6
 * indices du plus vague au plus précis. Le nom de la réponse est masqué
 * ("???") dans les indices.
 */
@Component
public class AoeTechTreeRiddleParser {

	private static final Set<String> HERO_INTERNAL_NAMES = Set.of("CAOCAO", "LIUBEI", "SUNJIAN");
	private static final Map<String, String> RESOURCES = Map.of("Food", "nourriture", "Wood", "bois", "Gold", "or",
			"Stone", "pierre");
	/** Les textes d'aide des unités sont décalés de 21000 par rapport à leur nom. */
	private static final int UNIT_HELP_OFFSET = 21000;

	private static final Pattern BLOCK_SEPARATOR = Pattern.compile("<br>\\s*\\n?\\s*<br>",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern BOLD = Pattern.compile("<b>(.*?)</b>");
	private static final Pattern UNIQUE_UNITS = Pattern.compile("^Unités? uniques?", flags());
	private static final Pattern UNIQUE_TECHS = Pattern.compile("^(Technologies|Techniques) uniques", flags());
	private static final Pattern TEAM_BONUS = Pattern.compile("^Bonus d'équipe", flags());
	private static final Pattern SEVERAL_UNIQUE_UNITS = Pattern.compile("<b>Unités uniques", flags());

	private record CivHelp(String type, List<String> bonuses, List<String> uniqueUnits, List<String> uniqueTechs,
			List<String> teamBonus, boolean hasSeveralUniqueUnits) {
	}

	private static int flags() {
		return Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	}

	public DailyRiddleData parse(JsonNode data, JsonNode strings) {
		if (data == null || !data.has("civs") || !data.path("data").has("Unit")) {
			throw new IllegalArgumentException("data.json invalide : clés 'civs' ou 'data.Unit' absentes.");
		}
		return new DailyRiddleData(buildCivilizations(data, strings), buildUnits(data, strings));
	}

	// ---------- Civilisations ----------

	private List<Entry> buildCivilizations(JsonNode data, JsonNode strings) {
		List<Entry> entries = new ArrayList<>();
		for (JsonNode civ : iterable(data.get("civs").elements())) {
			String name = text(strings, civ.path("name_string_id").asText());
			String raw = text(strings, civ.path("help_string_id").asText());
			if (name.isEmpty() || raw.isEmpty()) {
				continue;
			}
			CivHelp help = parseCivHelp(raw);
			String first = help.bonuses().isEmpty() ? "" : help.bonuses().get(0);
			String second = help.bonuses().size() > 1 ? help.bonuses().get(help.bonuses().size() - 1) : first;

			List<Hint> hints = List.of(new Hint("Type", help.type()),
					new Hint("Bonus d'équipe", String.join(" · ", help.teamBonus())),
					new Hint("Bonus de civilisation", second), new Hint("Autre bonus", first),
					new Hint("Technologies uniques", String.join(" · ", help.uniqueTechs())),
					new Hint(help.hasSeveralUniqueUnits() ? "Unités uniques" : "Unité unique",
							String.join(" · ", help.uniqueUnits())));
			entries.add(new Entry(name,
					hints.stream().map(h -> new Hint(h.label(), redact(h.text(), List.of(name)))).toList()));
		}
		return entries;
	}

	private CivHelp parseCivHelp(String raw) {
		List<List<String>> blocks = BLOCK_SEPARATOR.splitAsStream(raw).map(AoeTechTreeRiddleParser::trim)
				.map(AoeTechTreeRiddleParser::lines).toList();
		List<String> header = blocks.isEmpty() ? List.of() : blocks.get(0);
		return new CivHelp(header.isEmpty() ? "" : header.get(0), blocks.size() > 1 ? blocks.get(1) : List.of(),
				section(blocks, UNIQUE_UNITS), section(blocks, UNIQUE_TECHS), section(blocks, TEAM_BONUS),
				SEVERAL_UNIQUE_UNITS.matcher(raw).find());
	}

	/** Lignes d'un bloc dont la première ligne (le titre) correspond au motif, titre exclu. */
	private static List<String> section(List<List<String>> blocks, Pattern title) {
		return blocks.stream().filter(b -> !b.isEmpty() && title.matcher(b.get(0)).find()).findFirst()
				.map(b -> b.subList(1, b.size())).orElse(List.of());
	}

	private static List<String> lines(String block) {
		return stripHtml(block).lines().map(l -> trim(l.replaceFirst("(?U)^•\\s*", ""))).filter(l -> !l.isEmpty())
				.toList();
	}

	// ---------- Unités ----------

	private static final class UnitCandidate {
		final String name;
		final JsonNode unit;
		final String help;
		final Set<String> civs;

		UnitCandidate(String name, JsonNode unit, String help, Set<String> civs) {
			this.name = name;
			this.unit = unit;
			this.help = help;
			this.civs = civs;
		}
	}

	private List<Entry> buildUnits(JsonNode data, JsonNode strings) {
		Map<String, String> civNameOf = new LinkedHashMap<>();
		Map<String, Set<String>> civsByUnit = new LinkedHashMap<>();
		data.get("civs").properties().forEach(civ -> {
			civNameOf.put(civ.getKey(), text(strings, civ.getValue().path("name_string_id").asText()));
			for (JsonNode id : civ.getValue().path("Unit")) {
				civsByUnit.computeIfAbsent(id.asText(), k -> new LinkedHashSet<>()).add(civ.getKey());
			}
		});

		Map<String, UnitCandidate> byName = new LinkedHashMap<>();
		data.path("data").path("Unit").properties().forEach(field -> {
			JsonNode unit = field.getValue();
			Set<String> civs = civsByUnit.get(field.getKey());
			if (civs == null || HERO_INTERNAL_NAMES.contains(unit.path("internal_name").asText())) {
				return;
			}
			// Les unités sans texte d'aide (ex. Ratha) ne donneraient pas assez d'indices.
			String help = text(strings, String.valueOf(unit.path("LanguageNameId").asInt() + UNIT_HELP_OFFSET));
			Matcher bold = BOLD.matcher(help);
			if (!bold.find() || bold.group(1).toLowerCase(Locale.FRENCH).contains("élite")) {
				return;
			}
			String name = capitalize(trim(bold.group(1).replaceFirst("(?U)\\s*\\(.*\\)\\s*$", "")));
			UnitCandidate existing = byName.get(name);
			if (existing != null) {
				// Variantes d'une même unité (ex. lancier sicilien) : on fusionne les civs.
				existing.civs.addAll(civs);
			} else {
				byName.put(name, new UnitCandidate(name, unit, help, new LinkedHashSet<>(civs)));
			}
		});

		Collator collator = Collator.getInstance(Locale.FRENCH);
		int totalCivs = civNameOf.size();
		return byName.values().stream().map(c -> {
			List<String> rest = stripHtml(c.help).lines().filter(l -> !l.isEmpty()).skip(1).toList();
			String improvements = rest.stream().filter(l -> l.startsWith("Améliorations")).findFirst().orElse("");
			String description = rest.stream().filter(l -> !l.equals(improvements)).collect(Collectors.joining(" "));
			List<String> civNames = c.civs.stream().map(civNameOf::get).sorted(collator).toList();
			List<String> hidden = List.of(c.name, c.name + " d'élite");

			return new Entry(c.name, List.of(new Hint("Catégorie", categoryOf(c.unit)),
					new Hint("Coût", formatCost(c.unit)), new Hint("Statistiques de base", formatStats(c.unit)),
					new Hint("Disponibilité", formatAvailability(civNames, totalCivs)),
					new Hint("Améliorations",
							redact(improvements.replaceFirst("(?U)^Améliorations\\s*:\\s*", ""), hidden)),
					new Hint("Description", redact(description, hidden))));
		}).toList();
	}

	private static String categoryOf(JsonNode unit) {
		Set<Integer> classes = StreamSupport.stream(unit.path("Armours").spliterator(), false)
				.map(a -> a.path("Class").asInt()).collect(Collectors.toSet());
		if (classes.contains(16)) return "Navire";
		if (classes.contains(25)) return "Unité religieuse";
		if (classes.contains(20) || classes.contains(17)) return "Arme de siège";
		if (classes.contains(28)) return "Archer monté";
		if (classes.contains(15)) return "Unité à distance à pied";
		if (classes.contains(8) || classes.contains(30)) return "Cavalerie";
		if (classes.contains(1) || classes.contains(29)) return "Infanterie";
		return "Unité de soutien";
	}

	private static String formatCost(JsonNode unit) {
		List<String> parts = new ArrayList<>();
		unit.path("Cost").properties().forEach(
				cost -> parts.add(cost.getValue().asText() + " " + RESOURCES.getOrDefault(cost.getKey(), cost.getKey())));
		return String.join(", ", parts) + " · " + unit.path("TrainTime").asText() + " s de création";
	}

	private static String formatStats(JsonNode unit) {
		return String.join(" · ", "PV : " + unit.path("HP").asText(), "Attaque : " + unit.path("Attack").asText(),
				"Armure : " + unit.path("MeleeArmor").asText() + "/" + unit.path("PierceArmor").asText(),
				"Portée : " + unit.path("Range").asText(), "Vitesse : " + unit.path("Speed").asText());
	}

	private static String formatAvailability(List<String> civNames, int totalCivs) {
		if (civNames.size() == 1) {
			return "Unité unique : une seule civilisation y a accès";
		}
		if (civNames.size() <= 5) {
			return "Unité régionale, disponible pour " + civNames.size() + " civilisations : "
					+ String.join(", ", civNames);
		}
		if (civNames.size() == totalCivs) {
			return "Disponible pour toutes les civilisations";
		}
		return "Disponible pour " + civNames.size() + " civilisations sur " + totalCivs;
	}

	// ---------- Utilitaires texte ----------

	private static String text(JsonNode strings, String id) {
		return strings.path(id).asText("");
	}

	static String stripHtml(String text) {
		String plain = text.replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("<[^>]+>", "").replaceAll("‹[^›]*›", "")
				.replace('’', '\'').replaceAll("[ \\t]+", " ");
		return trim(plain.lines().map(AoeTechTreeRiddleParser::trim).collect(Collectors.joining("\n")));
	}

	/** Contrairement à String.trim(), retire aussi les espaces insécables présentes dans les textes FR. */
	private static String trim(String value) {
		return value.replaceAll("(?U)^\\s+|\\s+$", "");
	}

	/** Remplace le nom uniquement en mot entier : "archers" reste lisible pour l'Archer. */
	static String redact(String text, List<String> names) {
		String result = text;
		for (String name : names) {
			result = Pattern.compile("(?<!\\p{L})" + Pattern.quote(name) + "(?!\\p{L})", flags()).matcher(result)
					.replaceAll("???");
		}
		return result;
	}

	private static String capitalize(String s) {
		return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.FRENCH) + s.substring(1);
	}

	private static <T> Iterable<T> iterable(Iterator<T> iterator) {
		return () -> iterator;
	}
}
