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
 *
 * La difficulté doit monter progressivement : les indices qui suffisent
 * presque seuls à trouver la réponse (bonus d'équipe, coût exact, unité
 * unique...) viennent en dernier, les premiers ne font que réduire le champ.
 */
@Component
public class AoeTechTreeRiddleParser {

	private static final Set<String> HERO_INTERNAL_NAMES = Set.of("CAOCAO", "LIUBEI", "SUNJIAN");
	private static final Map<String, String> RESOURCES = Map.of("Food", "nourriture", "Wood", "bois", "Gold", "or",
			"Stone", "pierre");
	private static final Map<String, String> RESOURCES_PARTITIVE = Map.of("Food", "de la nourriture", "Wood",
			"du bois", "Gold", "de l'or", "Stone", "de la pierre");
	/** Les textes d'aide des unités sont décalés de 21000 par rapport à leur nom. */
	private static final int UNIT_HELP_OFFSET = 21000;
	/** Décalage entre LanguageNameId et le nom affiché, pour les unités et les technologies. */
	private static final int UNIT_NAME_OFFSET = 9000;
	private static final int TECH_NAME_OFFSET = 10000;
	/**
	 * Une unité ou technologie est "courante" si au moins cette part des civs y a
	 * accès : son absence est alors un indice parlant.
	 */
	private static final double COMMON_SHARE = 0.75;
	/** Au-delà, la liste des civs d'une unité est trop longue : on donne ses améliorations. */
	private static final int MAX_CIVS_LISTED = 8;

	private static final Pattern BLOCK_SEPARATOR = Pattern.compile("<br>\\s*\\n?\\s*<br>",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern BOLD = Pattern.compile("<b>(.*?)</b>");
	private static final Pattern UNIQUE_UNITS = Pattern.compile("^Unités? uniques?", flags());
	private static final Pattern UNIQUE_TECHS = Pattern.compile("^(Technologies|Techniques) uniques", flags());
	private static final Pattern TEAM_BONUS = Pattern.compile("^Bonus d'équipe", flags());
	private static final Pattern SEVERAL_UNIQUE_UNITS = Pattern.compile("<b>Unités uniques", flags());
	/**
	 * Adjectifs de civilisation ("Archer monté mongol unique") : retirés des
	 * descriptions d'unités, sinon l'indice "Points forts et faibles" donnerait la
	 * civ (et donc l'unité unique) bien avant le dernier indice.
	 */
	private static final Pattern DEMONYMS = Pattern.compile("\\s+(?:arm[ée]nien(?:ne)?|azt[èe]que|berb[èe]re"
			+ "|birmane?|boh[ée]mien(?:ne)?|bourguignon(?:ne)?|britannique|bulgare|byzantine?|celte|chinoise?"
			+ "|cor[ée]en(?:ne)?|coumane?|danoise?|dravidien(?:ne)?|espagnole?|franque?|gothique|gurjara"
			+ "|g[ée]orgien(?:ne)?|hindoustanie?|hongroise?|hunnique|inca|italien(?:ne)?|japonaise?|j[üu]rchen"
			+ "|khitane?|khm[èe]re?|lituanien(?:ne)?|malaise?|malien(?:ne)?|mapuche|maya|mongole?|muisca|perse"
			+ "|polonaise?|portugaise?|romaine?|sarrasine?|saxon(?:ne)?|sicilien(?:ne)?|slave|tatare?|teutonique"
			+ "|tupi|turque?|turc|var[èe]gue|vietnamien(?:ne)?|viking|[ée]thiopien(?:ne)?|bengalie?|shu|wei|wu)s?"
			+ "(?!\\p{L})", flags());

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
		List<String> commonUnits = commonIds(data, "Unit");
		List<String> commonTechs = commonIds(data, "Tech");

		List<Entry> entries = new ArrayList<>();
		for (JsonNode civ : iterable(data.get("civs").elements())) {
			String name = text(strings, civ.path("name_string_id").asText());
			String raw = text(strings, civ.path("help_string_id").asText());
			if (name.isEmpty() || raw.isEmpty()) {
				continue;
			}
			CivHelp help = parseCivHelp(raw);
			// Le premier bonus listé est le plus emblématique de la civ : on garde le
			// dernier, moins connu, pour ne pas donner la réponse trop tôt.
			String lesserBonus = help.bonuses().isEmpty() ? "" : help.bonuses().get(help.bonuses().size() - 1);

			List<Hint> hints = List.of(new Hint("Spécialité", help.type()),
					new Hint("Arbre technologique", formatMissing(data, strings, civ, commonUnits, commonTechs)),
					new Hint("Bonus de civilisation", lesserBonus),
					new Hint("Bonus d'équipe", String.join(" · ", help.teamBonus())),
					new Hint("Technologies uniques", String.join(" · ", help.uniqueTechs())),
					new Hint(help.hasSeveralUniqueUnits() ? "Unités uniques" : "Unité unique",
							String.join(" · ", help.uniqueUnits())));
			entries.add(new Entry(name,
					hints.stream().map(h -> new Hint(h.label(), redact(h.text(), List.of(name)))).toList()));
		}
		return entries;
	}

	/** Identifiants (unités ou technologies) auxquels au moins {@link #COMMON_SHARE} des civs ont accès. */
	private static List<String> commonIds(JsonNode data, String kind) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		int civCount = 0;
		for (JsonNode civ : iterable(data.get("civs").elements())) {
			civCount++;
			for (JsonNode id : civ.path(kind)) {
				counts.merge(id.asText(), 1, Integer::sum);
			}
		}
		double threshold = civCount * COMMON_SHARE;
		return counts.entrySet().stream().filter(e -> e.getValue() >= threshold).map(Map.Entry::getKey).toList();
	}

	/** Unités puis technologies courantes absentes de l'arbre de la civ. */
	private static String formatMissing(JsonNode data, JsonNode strings, JsonNode civ, List<String> commonUnits,
			List<String> commonTechs) {
		List<String> missing = new ArrayList<>();
		missing.addAll(missingNames(data, strings, civ, "Unit", commonUnits, UNIT_NAME_OFFSET));
		missing.addAll(missingNames(data, strings, civ, "Tech", commonTechs, TECH_NAME_OFFSET));
		if (missing.isEmpty()) {
			return "Aucune absence notable : arbre technologique presque complet";
		}
		return "N'a pas accès à : " + String.join(", ", missing);
	}

	private static List<String> missingNames(JsonNode data, JsonNode strings, JsonNode civ, String kind,
			List<String> commonIds, int nameOffset) {
		Set<String> owned = StreamSupport.stream(civ.path(kind).spliterator(), false).map(JsonNode::asText)
				.collect(Collectors.toSet());
		JsonNode definitions = data.path("data").path(kind);
		return commonIds.stream().filter(id -> !owned.contains(id)).map(id -> {
			JsonNode def = definitions.path(id);
			String label = def.has("LanguageNameId")
					? text(strings, String.valueOf(def.path("LanguageNameId").asInt() + nameOffset))
					: "";
			// Certains noms contiennent un retour à la ligne (<br>) pour l'arbre
			return collapseSpaces(stripHtml(label));
		}).filter(label -> !label.isEmpty()).distinct().toList();
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

			String upgrades = redact(improvements.replaceFirst("(?U)^Améliorations\\s*:\\s*", ""), hidden);

			return new Entry(c.name, List.of(new Hint("Catégorie", categoryOf(c.unit)),
					new Hint("Disponibilité", formatAvailability(civNames.size(), totalCivs)),
					new Hint("Ressources", formatResources(c.unit)),
					new Hint("Points forts et faibles",
							collapseSpaces(DEMONYMS.matcher(redact(description, hidden)).replaceAll(""))),
					new Hint("Coût et statistiques", formatCost(c.unit) + " · " + formatStats(c.unit)),
					civNames.size() <= MAX_CIVS_LISTED
							? new Hint("Civilisations", String.join(", ", civNames)
									+ (upgrades.isEmpty() ? "" : " · Améliorations : " + upgrades))
							: new Hint("Améliorations", upgrades)));
		}).toList();
	}

	/** Types de ressources seulement, sans les montants. */
	private static String formatResources(JsonNode unit) {
		List<String> parts = new ArrayList<>();
		unit.path("Cost").properties().forEach(cost -> parts.add(
				RESOURCES_PARTITIVE.getOrDefault(cost.getKey(), RESOURCES.getOrDefault(cost.getKey(), cost.getKey()))));
		if (parts.isEmpty()) {
			return "Ne coûte aucune ressource";
		}
		if (parts.size() == 1) {
			return "Coûte uniquement " + parts.get(0);
		}
		return "Coûte " + String.join(", ", parts.subList(0, parts.size() - 1)) + " et "
				+ parts.get(parts.size() - 1);
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

	/** Nombre de civs seulement : leurs noms ne viennent qu'au dernier indice. */
	private static String formatAvailability(int civCount, int totalCivs) {
		if (civCount == 1) {
			return "Unité unique : une seule civilisation y a accès";
		}
		if (civCount <= 5) {
			return "Unité régionale, disponible pour " + civCount + " civilisations";
		}
		if (civCount == totalCivs) {
			return "Disponible pour toutes les civilisations";
		}
		return "Disponible pour " + civCount + " civilisations sur " + totalCivs;
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

	/** Une seule espace entre les mots (retours à la ligne compris). */
	private static String collapseSpaces(String value) {
		return trim(value.replaceAll("\\s+", " "));
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
