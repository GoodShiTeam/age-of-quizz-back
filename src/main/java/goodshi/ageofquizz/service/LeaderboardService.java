package goodshi.ageofquizz.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goodshi.ageofquizz.dto.DailyRiddleStatsDTO;
import goodshi.ageofquizz.dto.LeaderboardDTO;
import goodshi.ageofquizz.dto.LeaderboardDTO.Entry;
import goodshi.ageofquizz.entity.GamePlayerResultEntity;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.repository.AnswerRepository;
import goodshi.ageofquizz.repository.DailyRiddleResultRepository;
import goodshi.ageofquizz.repository.GamePlayerResultRepository;
import goodshi.ageofquizz.repository.UserAnswerRepository;
import goodshi.ageofquizz.repository.UserRepository;
import goodshi.ageofquizz.service.AnswerAttempts.AnswerRow;
import goodshi.ageofquizz.service.AnswerAttempts.Attempt;

/**
 * Classements généraux, recalculés au plus toutes les {@link #CACHE_TTL} : ils
 * parcourent les réponses de tous les joueurs.
 */
@Service
public class LeaderboardService {

	public static final int MIN_QUESTIONS_FOR_ACCURACY = 50;
	private static final int TOP = 20;
	private static final Duration CACHE_TTL = Duration.ofMinutes(5);
	/** Compte technique des joueurs non connectés. */
	private static final long ANONYMOUS_USER_ID = -1L;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserAnswerRepository userAnswerRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private GamePlayerResultRepository gamePlayerResultRepository;

	@Autowired
	private DailyRiddleResultRepository dailyRiddleResultRepository;

	@Autowired
	private DailyRiddleService dailyRiddleService;

	private volatile LeaderboardDTO cached;

	@Transactional(readOnly = true)
	public LeaderboardDTO getLeaderboard() {
		LeaderboardDTO current = cached;
		if (current != null && current.computedAt().plus(CACHE_TTL).isAfter(Instant.now())) {
			return current;
		}
		synchronized (this) {
			if (cached == null || cached.computedAt().plus(CACHE_TTL).isBefore(Instant.now())) {
				cached = compute();
			}
			return cached;
		}
	}

	/** Score d'un joueur pour un classement. */
	private record Score(Long userId, double value, int sampleSize) {
	}

	private LeaderboardDTO compute() {
		List<Score> accuracy = new ArrayList<>();
		List<Score> questions = new ArrayList<>();
		computeAnswerScores(accuracy, questions);

		List<Score> wins = computeWins();
		List<Score> streaks = computeDailyStreaks();

		List<Long> userIds = new ArrayList<>();
		for (List<Score> scores : List.of(accuracy, questions, wins, streaks)) {
			scores.forEach(s -> userIds.add(s.userId()));
		}
		Map<Long, User> users = new HashMap<>();
		userRepository.findAllById(userIds).forEach(u -> users.put(u.getId(), u));

		return new LeaderboardDTO(toEntries(accuracy, users), toEntries(questions, users), toEntries(wins, users),
				toEntries(streaks, users), MIN_QUESTIONS_FOR_ACCURACY, Instant.now());
	}

	private void computeAnswerScores(List<Score> accuracy, List<Score> questions) {
		Map<Long, List<AnswerRow>> rowsByUser = new HashMap<>();
		for (Object[] row : userAnswerRepository.findAllAnswerRows()) {
			Long userId = (Long) row[0];
			if (userId == null || userId == ANONYMOUS_USER_ID) {
				continue;
			}
			rowsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(new AnswerRow((Integer) row[1],
					(Integer) row[2], (LocalDateTime) row[3], (BigDecimal) row[4]));
		}
		if (rowsByUser.isEmpty()) {
			return;
		}

		var correctIds = AnswerAttempts.correctIdsByQuestion(answerRepository.findAllCorrectAnswerIds());
		for (Map.Entry<Long, List<AnswerRow>> e : rowsByUser.entrySet()) {
			List<Attempt> attempts = AnswerAttempts.group(e.getValue(), correctIds);
			int answered = attempts.size();
			int correct = (int) attempts.stream().filter(Attempt::correct).count();
			questions.add(new Score(e.getKey(), answered, answered));
			if (answered >= MIN_QUESTIONS_FOR_ACCURACY) {
				accuracy.add(new Score(e.getKey(), UserStatsService.round(100.0 * correct / answered), answered));
			}
		}
	}

	private List<Score> computeWins() {
		Map<String, Long> playerCounts = UserStatsService
				.countPlayers(gamePlayerResultRepository.countPlayersByAllSessions());
		Map<Long, int[]> byUser = new HashMap<>();
		for (GamePlayerResultEntity r : gamePlayerResultRepository.findAll()) {
			if (r.getUser() == null || r.getUser().getId() == null || r.getUser().getId() == ANONYMOUS_USER_ID) {
				continue;
			}
			int[] counts = byUser.computeIfAbsent(r.getUser().getId(), k -> new int[2]);
			counts[1]++;
			int rank = r.getRank() == null ? 0 : r.getRank();
			if (UserStatsService.isWin(rank, playerCounts.getOrDefault(r.getGameSessionId(), 1L).intValue())) {
				counts[0]++;
			}
		}
		List<Score> scores = new ArrayList<>();
		byUser.forEach((userId, counts) -> {
			if (counts[0] > 0) {
				scores.add(new Score(userId, counts[0], counts[1]));
			}
		});
		return scores;
	}

	private List<Score> computeDailyStreaks() {
		List<Score> scores = new ArrayList<>();
		for (User user : dailyRiddleResultRepository.findDistinctUsers()) {
			DailyRiddleStatsDTO stats = dailyRiddleService.getStats(user);
			if (stats.maxStreak() > 0) {
				scores.add(new Score(user.getId(), stats.maxStreak(), stats.played()));
			}
		}
		return scores;
	}

	/**
	 * Trie par valeur puis par base de calcul, garde le top {@link #TOP} et
	 * attribue les rangs (ex aequo = même rang).
	 */
	private List<Entry> toEntries(List<Score> scores, Map<Long, User> users) {
		ToDoubleFunction<Score> value = Score::value;
		ToIntFunction<Score> sample = Score::sampleSize;
		List<Score> sorted = scores.stream().filter(s -> hasProfile(users.get(s.userId())))
				.sorted(Comparator.comparingDouble(value).reversed().thenComparing(
						Comparator.comparingInt(sample).reversed()))
				.limit(TOP).toList();

		List<Entry> entries = new ArrayList<>();
		for (int i = 0; i < sorted.size(); i++) {
			Score s = sorted.get(i);
			int rank = i > 0 && s.value() == sorted.get(i - 1).value() ? entries.get(i - 1).rank() : i + 1;
			User u = users.get(s.userId());
			entries.add(new Entry(rank, u.getUserProfile().getId(), u.getUserProfile().getPseudo(),
					u.getUserProfile().getAvatar(), s.value(), s.sampleSize()));
		}
		return entries;
	}

	private static boolean hasProfile(User user) {
		return user != null && user.getUserProfile() != null;
	}
}
