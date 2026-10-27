package goodshi.ageofquizz.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import goodshi.ageofquizz.dto.DailyRiddleEntryDTO;
import goodshi.ageofquizz.dto.DailyRiddleEntryRequestDTO;
import goodshi.ageofquizz.dto.DailyRiddleImportResultDTO;
import goodshi.ageofquizz.entity.DailyRiddleEntry;
import goodshi.ageofquizz.entity.DailyRiddleHint;
import goodshi.ageofquizz.entity.DailyRiddleSchedule;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.model.DailyRiddleData;
import goodshi.ageofquizz.model.DailyRiddleData.Entry;
import goodshi.ageofquizz.model.DailyRiddleData.Hint;
import goodshi.ageofquizz.model.DailyRiddleData.TargetType;
import goodshi.ageofquizz.repository.DailyRiddleEntryRepository;
import goodshi.ageofquizz.repository.DailyRiddleScheduleRepository;

/**
 * Gestion des énigmes en base : import depuis les fichiers aoe2techtree,
 * édition manuelle et remplissage initial au premier démarrage.
 */
@Service
public class DailyRiddleAdminService {

	private static final Logger logger = LoggerFactory.getLogger(DailyRiddleAdminService.class);

	/** Jeu de données livré avec l'application, utilisé si la base est vide. */
	private static final String SEED_PATH = "daily-riddle/riddle-data.json";
	private static final int MIN_HINTS = 3;
	private static final int MAX_LABEL_LENGTH = 100;
	private static final int MAX_TEXT_LENGTH = 1000;

	private final DailyRiddleEntryRepository entryRepository;
	private final DailyRiddleScheduleRepository scheduleRepository;
	private final AoeTechTreeRiddleParser parser;
	private final ObjectMapper objectMapper;

	public DailyRiddleAdminService(DailyRiddleEntryRepository entryRepository,
			DailyRiddleScheduleRepository scheduleRepository, AoeTechTreeRiddleParser parser,
			ObjectMapper objectMapper) {
		this.entryRepository = entryRepository;
		this.scheduleRepository = scheduleRepository;
		this.parser = parser;
		this.objectMapper = objectMapper;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void seedIfEmpty() throws IOException {
		if (entryRepository.count() > 0) {
			return;
		}
		try (InputStream in = new ClassPathResource(SEED_PATH).getInputStream()) {
			DailyRiddleImportResultDTO result = importData(objectMapper.readValue(in, DailyRiddleData.class), true);
			logger.info("Énigmes initialisées : {} civilisations, {} unités", result.civilizations(),
					result.units());
		}
	}

	@Transactional
	public DailyRiddleImportResultDTO importFromTechTree(InputStream dataJson, InputStream frenchStrings,
			boolean keepManualEdits) throws IOException {
		JsonNode data = objectMapper.readTree(dataJson);
		JsonNode strings = objectMapper.readTree(frenchStrings);
		return importData(parser.parse(data, strings), keepManualEdits);
	}

	private DailyRiddleImportResultDTO importData(DailyRiddleData data, boolean keepManualEdits) {
		int[] counts = new int[3]; // created, updated, skipped
		List<String> warnings = new ArrayList<>();
		int civilizations = upsert(TargetType.CIVILIZATION, data.civilizations(), keepManualEdits, counts, warnings);
		int units = upsert(TargetType.UNIT, data.units(), keepManualEdits, counts, warnings);
		return new DailyRiddleImportResultDTO(civilizations, units, counts[0], counts[1], counts[2], warnings);
	}

	/** Met à jour par nom ; les entrées absentes de l'import (énigmes perso) ne sont pas touchées. */
	private int upsert(TargetType type, List<Entry> entries, boolean keepManualEdits, int[] counts,
			List<String> warnings) {
		Map<String, DailyRiddleEntry> existing = entryRepository.findByType(type).stream()
				.collect(Collectors.toMap(DailyRiddleEntry::getName, Function.identity()));
		int imported = 0;
		for (Entry entry : entries) {
			List<Hint> hints = entry.hints().stream().filter(h -> h.text() != null && !h.text().isBlank())
					.map(h -> new Hint(truncate(h.label(), MAX_LABEL_LENGTH), truncate(h.text(), MAX_TEXT_LENGTH)))
					.toList();
			if (hints.size() < MIN_HINTS) {
				warnings.add(entry.name() + " : ignorée (seulement " + hints.size() + " indice(s))");
				continue;
			}
			if (hints.size() < entry.hints().size()) {
				warnings.add(entry.name() + " : " + (entry.hints().size() - hints.size()) + " indice(s) vide(s) retiré(s)");
			}
			imported++;

			DailyRiddleEntry target = existing.get(entry.name());
			if (target == null) {
				target = new DailyRiddleEntry();
				target.setName(entry.name());
				target.setType(type);
				counts[0]++;
			} else if (keepManualEdits && target.isManuallyEdited()) {
				counts[2]++;
				continue;
			} else {
				counts[1]++;
			}
			target.setManuallyEdited(false);
			target.replaceHints(toEntities(hints));
			entryRepository.save(target);
		}
		return imported;
	}

	@Transactional(readOnly = true)
	public List<DailyRiddleEntryDTO> listEntries() {
		Map<Integer, LocalDate> lastPlayed = scheduleRepository.findAll().stream().collect(Collectors.toMap(
				s -> s.getEntry().getId(), DailyRiddleSchedule::getDate, (a, b) -> a.isAfter(b) ? a : b));
		return entryRepository.findAll().stream()
				.sorted(Comparator.comparing(DailyRiddleEntry::getType).thenComparing(DailyRiddleEntry::getName))
				.map(e -> toDTO(e, lastPlayed.get(e.getId()))).toList();
	}

	@Transactional
	public DailyRiddleEntryDTO createEntry(DailyRiddleEntryRequestDTO request) {
		validate(request);
		String name = request.name().trim();
		if (entryRepository.findByTypeAndName(request.type(), name).isPresent()) {
			throw new IllegalArgumentException("Une énigme « " + name + " » existe déjà pour ce type.");
		}
		DailyRiddleEntry entry = new DailyRiddleEntry();
		entry.setName(name);
		entry.setType(request.type());
		return save(entry, request);
	}

	@Transactional
	public DailyRiddleEntryDTO updateEntry(int id, DailyRiddleEntryRequestDTO request) {
		validate(request);
		DailyRiddleEntry entry = entryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Énigme introuvable : " + id));
		// Le nom et le type sont la clé de l'import : on ne les change pas après coup.
		return save(entry, request);
	}

	private DailyRiddleEntryDTO save(DailyRiddleEntry entry, DailyRiddleEntryRequestDTO request) {
		entry.setActive(request.active());
		entry.setManuallyEdited(true);
		entry.replaceHints(toEntities(request.hints().stream()
				.map(h -> new Hint(h.label().trim(), h.text().trim())).toList()));
		DailyRiddleEntry saved = entryRepository.save(entry);
		return toDTO(saved,
				scheduleRepository.findFirstByEntryOrderByDateDesc(saved).map(DailyRiddleSchedule::getDate).orElse(null));
	}

	private static void validate(DailyRiddleEntryRequestDTO request) {
		if (request.name() == null || request.name().isBlank() || request.type() == null) {
			throw new IllegalArgumentException("Le nom et le type sont obligatoires.");
		}
		if (request.hints() == null || request.hints().isEmpty()
				|| request.hints().size() > DailyRiddleService.MAX_ATTEMPTS) {
			throw new IllegalArgumentException(
					"Une énigme doit avoir entre 1 et " + DailyRiddleService.MAX_ATTEMPTS + " indices.");
		}
		for (Hint hint : request.hints()) {
			if (hint.label() == null || hint.label().isBlank() || hint.text() == null || hint.text().isBlank()) {
				throw new IllegalArgumentException("Chaque indice doit avoir un libellé et un texte.");
			}
			if (hint.label().length() > MAX_LABEL_LENGTH || hint.text().length() > MAX_TEXT_LENGTH) {
				throw new IllegalArgumentException("Indice trop long (libellé " + MAX_LABEL_LENGTH + ", texte "
						+ MAX_TEXT_LENGTH + " caractères max).");
			}
		}
	}

	private static List<DailyRiddleHint> toEntities(List<Hint> hints) {
		return hints.stream().map(h -> new DailyRiddleHint(h.label(), h.text())).collect(Collectors.toList());
	}

	private static DailyRiddleEntryDTO toDTO(DailyRiddleEntry entry, LocalDate lastPlayedOn) {
		return new DailyRiddleEntryDTO(entry.getId(), entry.getName(), entry.getType(), entry.isActive(),
				entry.isManuallyEdited(), lastPlayedOn,
				entry.getHints().stream().map(h -> new Hint(h.getLabel(), h.getText())).toList());
	}

	private static String truncate(String value, int max) {
		return value.length() <= max ? value : value.substring(0, max - 1) + "…";
	}
}
