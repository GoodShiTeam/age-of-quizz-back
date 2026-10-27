package goodshi.ageofquizz.service;

import java.security.SecureRandom;
import java.text.Collator;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import goodshi.ageofquizz.dto.DailyRiddleDTO;
import goodshi.ageofquizz.dto.DailyRiddleGuessResponse;
import goodshi.ageofquizz.dto.DailyRiddleProgressDTO;
import goodshi.ageofquizz.dto.DailyRiddleStatsDTO;
import goodshi.ageofquizz.entity.DailyRiddleEntry;
import goodshi.ageofquizz.entity.DailyRiddleResult;
import goodshi.ageofquizz.entity.DailyRiddleResult.Status;
import goodshi.ageofquizz.entity.DailyRiddleSchedule;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.model.DailyRiddleData.Hint;
import goodshi.ageofquizz.model.DailyRiddleData.TargetType;
import goodshi.ageofquizz.repository.DailyRiddleEntryRepository;
import goodshi.ageofquizz.repository.DailyRiddleResultRepository;
import goodshi.ageofquizz.repository.DailyRiddleScheduleRepository;

/**
 * Énigme journalière : une civilisation ou une unité à deviner à l'aide
 * d'indices débloqués à chaque tentative ratée.
 *
 * Les jours pairs (depuis FIRST_DAY) sont des civilisations, les jours impairs
 * des unités. L'énigme d'un jour est tirée au sort parmi les entrées actives
 * jamais jouées, puis enregistrée dans daily_riddle_schedule pour rester
 * stable. Quand tout a été joué, on repioche hors des 30 derniers jours.
 *
 * Pour un anonyme, la progression est tenue par le client (numéro de
 * tentative transmis). Pour un utilisateur connecté, elle est enregistrée dans
 * daily_riddle_result et le numéro de tentative transmis est ignoré.
 */
@Service
public class DailyRiddleService {

	public static final int MAX_ATTEMPTS = 6;

	private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
	private static final LocalDate FIRST_DAY = LocalDate.of(2026, 10, 27);

	private final DailyRiddleEntryRepository entryRepository;
	private final DailyRiddleScheduleRepository scheduleRepository;
	private final DailyRiddleResultRepository resultRepository;
	private final TransactionTemplate transactionTemplate;
	private final Random random = new SecureRandom();

	public DailyRiddleService(DailyRiddleEntryRepository entryRepository,
			DailyRiddleScheduleRepository scheduleRepository, DailyRiddleResultRepository resultRepository,
			PlatformTransactionManager transactionManager) {
		this.entryRepository = entryRepository;
		this.scheduleRepository = scheduleRepository;
		this.resultRepository = resultRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public LocalDate today() {
		return LocalDate.now(ZONE);
	}

	/** @param user utilisateur connecté, ou null pour un anonyme */
	public DailyRiddleDTO getRiddle(int revealedHints, User user) {
		LocalDate date = today();
		ensureScheduled(date);
		return transactionTemplate.execute(status -> {
			DailyRiddleEntry entry = scheduleRepository.getReferenceById(date).getEntry();
			List<Hint> hints = hintsOf(entry);
			DailyRiddleResult result = user == null ? null
					: resultRepository.findByUserAndDate(user, date).orElse(null);

			int count;
			DailyRiddleProgressDTO progress = null;
			if (result != null) {
				count = result.isFinished() ? hints.size() : revealedAfter(result.getGuesses().size(), hints);
				progress = new DailyRiddleProgressDTO(List.copyOf(result.getGuesses()), result.getStatus(),
						result.isFinished() ? entry.getName() : null);
			} else {
				count = Math.max(1, Math.min(revealedHints, hints.size()));
			}
			return new DailyRiddleDTO(date, numberOf(date), entry.getType(), MAX_ATTEMPTS, hints.size(),
					hints.subList(0, count), candidatesOf(entry.getType()), progress);
		});
	}

	/**
	 * @param attempt numéro de tentative tenu par le client, utilisé seulement
	 *                pour un anonyme
	 * @param user    utilisateur connecté, ou null pour un anonyme
	 */
	public DailyRiddleGuessResponse guess(String guess, int attempt, User user) {
		LocalDate date = today();
		ensureScheduled(date);
		return transactionTemplate.execute(status -> {
			DailyRiddleEntry entry = scheduleRepository.getReferenceById(date).getEntry();
			List<Hint> hints = hintsOf(entry);
			Map<String, String> candidates = candidatesOf(entry.getType()).stream()
					.collect(Collectors.toMap(DailyRiddleService::normalize, Function.identity(), (a, b) -> a));
			String normalizedGuess = normalize(guess);
			String candidate = candidates.get(normalizedGuess);

			if (user == null) {
				if (candidate == null) {
					return new DailyRiddleGuessResponse(false, false, null,
							hints.subList(0, revealedAfter(attempt - 1, hints)), null);
				}
				boolean correct = normalizedGuess.equals(normalize(entry.getName()));
				boolean finished = correct || attempt >= MAX_ATTEMPTS;
				return new DailyRiddleGuessResponse(true, correct, finished ? entry.getName() : null,
						hints.subList(0, finished ? hints.size() : revealedAfter(attempt, hints)), null);
			}

			DailyRiddleResult result = resultRepository.findByUserAndDate(user, date)
					.orElseGet(() -> new DailyRiddleResult(user, date));
			boolean alreadyGuessed = result.getGuesses().stream().map(DailyRiddleService::normalize)
					.anyMatch(normalizedGuess::equals);
			if (!result.isFinished() && candidate != null && !alreadyGuessed) {
				boolean correct = normalizedGuess.equals(normalize(entry.getName()));
				Status newStatus = correct ? Status.WON
						: result.getGuesses().size() + 1 >= MAX_ATTEMPTS ? Status.LOST : Status.PLAYING;
				result.addGuess(candidate, newStatus);
				resultRepository.save(result);
			} else if (!result.isFinished()) {
				// Proposition inconnue ou déjà faite : non comptée.
				return new DailyRiddleGuessResponse(false, false, null,
						hints.subList(0, revealedAfter(result.getGuesses().size(), hints)),
						List.copyOf(result.getGuesses()));
			}

			boolean finished = result.isFinished();
			return new DailyRiddleGuessResponse(true, result.getStatus() == Status.WON,
					finished ? entry.getName() : null,
					hints.subList(0, finished ? hints.size() : revealedAfter(result.getGuesses().size(), hints)),
					List.copyOf(result.getGuesses()));
		});
	}

	/**
	 * Une série = jours consécutifs gagnés. Une partie perdue, ou abandonnée un
	 * jour passé, casse la série ; la partie du jour en cours ne la casse pas.
	 */
	public DailyRiddleStatsDTO getStats(User user) {
		LocalDate date = today();
		List<DailyRiddleResult> results = resultRepository.findByUserOrderByDateAsc(user);
		List<Integer> distribution = new ArrayList<>(Collections.nCopies(MAX_ATTEMPTS, 0));
		int played = 0;
		int won = 0;
		int run = 0;
		int maxStreak = 0;
		LocalDate lastWon = null;

		for (DailyRiddleResult result : results) {
			boolean ongoingToday = !result.isFinished() && result.getDate().equals(date);
			if (ongoingToday) {
				continue;
			}
			played++;
			if (result.getStatus() == Status.WON) {
				won++;
				int attempts = Math.min(result.getGuesses().size(), MAX_ATTEMPTS);
				distribution.set(attempts - 1, distribution.get(attempts - 1) + 1);
				run = lastWon != null && lastWon.plusDays(1).equals(result.getDate()) ? run + 1 : 1;
				lastWon = result.getDate();
				maxStreak = Math.max(maxStreak, run);
			} else {
				run = 0;
				lastWon = null;
			}
		}
		int currentStreak = lastWon != null && !lastWon.isBefore(date.minusDays(1)) ? run : 0;
		return new DailyRiddleStatsDTO(played, won, currentStreak, maxStreak, distribution);
	}

	/** Un indice au départ, puis un de plus par tentative ratée. */
	private static int revealedAfter(int failedAttempts, List<Hint> hints) {
		return Math.max(1, Math.min(failedAttempts + 1, hints.size()));
	}

	private static int numberOf(LocalDate date) {
		return (int) ChronoUnit.DAYS.between(FIRST_DAY, date) + 1;
	}

	private void ensureScheduled(LocalDate date) {
		if (!scheduleRepository.existsById(date)) {
			createSchedule(date);
		}
	}

	/**
	 * synchronized + contrainte de clé primaire sur la date : si deux requêtes
	 * arrivent en même temps à minuit, une seule énigme est retenue.
	 */
	private synchronized void createSchedule(LocalDate date) {
		try {
			transactionTemplate.executeWithoutResult(status -> {
				if (!scheduleRepository.existsById(date)) {
					scheduleRepository.save(new DailyRiddleSchedule(date, pickEntry(date)));
				}
			});
		} catch (DataIntegrityViolationException e) {
			// Créée entre-temps par une autre instance : rien à faire.
		}
	}

	private DailyRiddleEntry pickEntry(LocalDate date) {
		long dayIndex = ChronoUnit.DAYS.between(FIRST_DAY, date);
		TargetType type = Math.floorMod(dayIndex, 2) == 0 ? TargetType.CIVILIZATION : TargetType.UNIT;

		List<DailyRiddleEntry> pool = entryRepository.findActiveNeverScheduled(type);
		if (pool.isEmpty()) {
			Set<Integer> recent = scheduleRepository.findTop30ByOrderByDateDesc().stream()
					.map(s -> s.getEntry().getId()).collect(Collectors.toSet());
			List<DailyRiddleEntry> active = entryRepository.findByTypeAndActiveTrue(type);
			pool = active.stream().filter(e -> !recent.contains(e.getId())).toList();
			if (pool.isEmpty()) {
				pool = active;
			}
		}
		if (pool.isEmpty()) {
			throw new ResourceNotFoundException("Aucune énigme active pour le type " + type);
		}
		return pool.get(random.nextInt(pool.size()));
	}

	private static List<Hint> hintsOf(DailyRiddleEntry entry) {
		return entry.getHints().stream().map(h -> new Hint(h.getLabel(), h.getText())).toList();
	}

	/** Toutes les entrées du type, actives ou non : une réponse désactivée reste proposable. */
	private List<String> candidatesOf(TargetType type) {
		Collator collator = Collator.getInstance(Locale.FRENCH);
		return entryRepository.findByType(type).stream().map(DailyRiddleEntry::getName).sorted(collator).toList();
	}

	static String normalize(String value) {
		if (value == null) {
			return "";
		}
		return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('’', '\'')
				.trim().toLowerCase(Locale.ROOT);
	}
}
