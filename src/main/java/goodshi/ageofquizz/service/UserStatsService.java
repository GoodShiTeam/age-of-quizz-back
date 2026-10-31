package goodshi.ageofquizz.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goodshi.ageofquizz.dto.DailyRiddleStatsDTO;
import goodshi.ageofquizz.dto.UserStatsDTO;
import goodshi.ageofquizz.dto.UserStatsDTO.AuthorStats;
import goodshi.ageofquizz.dto.UserStatsDTO.CategoryStat;
import goodshi.ageofquizz.dto.UserStatsDTO.MultiplayerStats;
import goodshi.ageofquizz.dto.UserStatsDTO.Overview;
import goodshi.ageofquizz.dto.UserStatsDTO.RecentGame;
import goodshi.ageofquizz.dto.UserStatsDTO.WeeklyProgress;
import goodshi.ageofquizz.entity.GamePlayerResultEntity;
import goodshi.ageofquizz.entity.GameSessionEntity;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.Question.QuestionCivilisation;
import goodshi.ageofquizz.entity.Question.QuestionStatus;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.entity.UserAnswer;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.repository.AnswerRepository;
import goodshi.ageofquizz.repository.GamePlayerResultRepository;
import goodshi.ageofquizz.repository.GameSessionRepository;
import goodshi.ageofquizz.repository.QuestionRepository;
import goodshi.ageofquizz.repository.UserAnswerRepository;
import goodshi.ageofquizz.repository.UserRepository;
import goodshi.ageofquizz.service.AnswerAttempts.AnswerRow;
import goodshi.ageofquizz.service.AnswerAttempts.Attempt;

@Service
public class UserStatsService {

	private static final int RECENT_GAMES = 10;
	private static final int PROGRESS_WEEKS = 12;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserAnswerRepository userAnswerRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private GamePlayerResultRepository gamePlayerResultRepository;

	@Autowired
	private GameSessionRepository gameSessionRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private DailyRiddleService dailyRiddleService;

	@Transactional(readOnly = true)
	public UserStatsDTO getStatsByProfileId(Long userProfileId) {
		User user = userRepository.findByUserProfileId(userProfileId)
				.orElseThrow(() -> new ResourceNotFoundException("Profil utilisateur non trouvé"));

		List<UserAnswer> answers = userAnswerRepository.findAllWithQuestionByUserId(user.getId());
		Map<Integer, Question> questions = new HashMap<>();
		List<AnswerRow> rows = new ArrayList<>();
		for (UserAnswer ua : answers) {
			questions.putIfAbsent(ua.getQuestion().getId(), ua.getQuestion());
			rows.add(new AnswerRow(ua.getQuestion().getId(), ua.getAnswer().getId(), ua.getAnsweredAt(),
					ua.getResponseTimeSeconds()));
		}
		List<Attempt> attempts = rows.isEmpty() ? List.of()
				: AnswerAttempts.group(rows, AnswerAttempts
						.correctIdsByQuestion(answerRepository.findCorrectAnswerIds(questions.keySet())));

		DailyRiddleStatsDTO dailyRiddle = dailyRiddleService.getStats(user);

		return new UserStatsDTO(buildOverview(attempts),
				buildCategoryStats(attempts, questions, q -> enumKey(q.getTheme())),
				buildCategoryStats(attempts, questions,
						q -> q.getCivilisation() == QuestionCivilisation.NONE ? null : enumKey(q.getCivilisation())),
				buildProgress(attempts), buildMultiplayerStats(user.getId()),
				dailyRiddle.played() > 0 ? dailyRiddle : null, buildAuthorStats(user.getId()));
	}

	private Overview buildOverview(List<Attempt> attempts) {
		int correct = (int) attempts.stream().filter(Attempt::correct).count();
		// Les questions sans réponse sont enregistrées avec un temps de 0
		double averageTime = attempts.stream().mapToDouble(Attempt::responseTimeSeconds).filter(t -> t > 0)
				.average().orElse(0);
		return new Overview(attempts.size(), correct, round(averageTime));
	}

	/** Une entrée par catégorie (null = ignorée), triée par nombre de réponses. */
	private List<CategoryStat> buildCategoryStats(List<Attempt> attempts, Map<Integer, Question> questions,
			Function<Question, String> category) {
		Map<String, int[]> counts = new LinkedHashMap<>();
		for (Attempt attempt : attempts) {
			String key = category.apply(questions.get(attempt.questionId()));
			if (key == null) {
				continue;
			}
			int[] count = counts.computeIfAbsent(key, k -> new int[2]);
			count[0]++;
			if (attempt.correct()) {
				count[1]++;
			}
		}
		return counts.entrySet().stream().map(e -> new CategoryStat(e.getKey(), e.getValue()[0], e.getValue()[1]))
				.sorted(Comparator.comparingInt(CategoryStat::answered).reversed()).toList();
	}

	/** Les {@link #PROGRESS_WEEKS} dernières semaines, semaine en cours comprise. */
	private List<WeeklyProgress> buildProgress(List<Attempt> attempts) {
		LocalDate currentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate firstWeek = currentWeek.minusWeeks(PROGRESS_WEEKS - 1);

		Map<LocalDate, int[]> counts = new LinkedHashMap<>();
		for (int i = 0; i < PROGRESS_WEEKS; i++) {
			counts.put(firstWeek.plusWeeks(i), new int[2]);
		}
		for (Attempt attempt : attempts) {
			if (attempt.answeredAt() == null) {
				continue;
			}
			LocalDate week = attempt.answeredAt().toLocalDate()
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			int[] count = counts.get(week);
			if (count == null) {
				continue;
			}
			count[0]++;
			if (attempt.correct()) {
				count[1]++;
			}
		}
		return counts.entrySet().stream()
				.map(e -> new WeeklyProgress(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();
	}

	private MultiplayerStats buildMultiplayerStats(Long userId) {
		List<GamePlayerResultEntity> results = gamePlayerResultRepository.findByUserId(userId);
		if (results.isEmpty()) {
			return new MultiplayerStats(0, 0, 0, 0, 0, 0, List.of());
		}

		Set<String> sessionIds = results.stream().map(GamePlayerResultEntity::getGameSessionId)
				.collect(Collectors.toSet());
		Map<String, GameSessionEntity> sessions = gameSessionRepository.findAllById(sessionIds).stream()
				.collect(Collectors.toMap(GameSessionEntity::getGameSessionId, s -> s));
		Map<String, Long> playerCounts = countPlayers(gamePlayerResultRepository.countPlayersBySession(sessionIds));

		int wins = 0;
		int podiums = 0;
		int perfectGames = 0;
		int bestPoints = 0;
		double accuracySum = 0;
		for (GamePlayerResultEntity r : results) {
			int rank = valueOrZero(r.getRank());
			int playerCount = playerCounts.getOrDefault(r.getGameSessionId(), 1L).intValue();
			if (isWin(rank, playerCount)) {
				wins++;
			}
			// Une partie seul ne compte ni comme victoire ni comme podium
			if (playerCount > 1 && rank >= 1 && rank <= 3) {
				podiums++;
			}
			int total = valueOrZero(r.getTotalQuestions());
			if (total > 0 && valueOrZero(r.getCorrectAnswers()) == total) {
				perfectGames++;
			}
			bestPoints = Math.max(bestPoints, valueOrZero(r.getPoints()));
			accuracySum += r.getAccuracy() == null ? 0 : r.getAccuracy();
		}

		List<RecentGame> recentGames = results.stream().map(r -> {
			GameSessionEntity session = sessions.get(r.getGameSessionId());
			Instant playedAt = session == null ? null : session.getStartTime();
			return new RecentGame(r.getGameSessionId(), playedAt, valueOrZero(r.getRank()),
					playerCounts.getOrDefault(r.getGameSessionId(), 1L).intValue(), valueOrZero(r.getPoints()),
					valueOrZero(r.getCorrectAnswers()), valueOrZero(r.getTotalQuestions()));
		}).sorted(Comparator.comparing(RecentGame::playedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(RECENT_GAMES).toList();

		return new MultiplayerStats(results.size(), wins, podiums, perfectGames, bestPoints,
				round(accuracySum / results.size()), recentGames);
	}

	/** Une partie seul ne compte pas comme victoire. */
	static boolean isWin(int rank, int playerCount) {
		return playerCount > 1 && rank == 1;
	}

	/** Paires [gameSessionId, nombre de joueurs] -> map. */
	static Map<String, Long> countPlayers(List<Object[]> pairs) {
		Map<String, Long> playerCounts = new HashMap<>();
		for (Object[] row : pairs) {
			playerCounts.put((String) row[0], (Long) row[1]);
		}
		return playerCounts;
	}

	private AuthorStats buildAuthorStats(Long userId) {
		Map<QuestionStatus, Integer> byStatus = new HashMap<>();
		for (Object[] row : questionRepository.countByAuthorGroupByStatus(userId)) {
			byStatus.put((QuestionStatus) row[0], ((Long) row[1]).intValue());
		}
		int created = byStatus.values().stream().mapToInt(Integer::intValue).sum();
		if (created == 0) {
			return null;
		}
		return new AuthorStats(created, byStatus.getOrDefault(QuestionStatus.VALIDATED, 0),
				byStatus.getOrDefault(QuestionStatus.CREATED_REVIEW, 0)
						+ byStatus.getOrDefault(QuestionStatus.CLAIMED_REVIEW, 0),
				byStatus.getOrDefault(QuestionStatus.REJECTED, 0));
	}

	private static String enumKey(Enum<?> value) {
		return value == null ? null : value.name();
	}

	private static int valueOrZero(Integer value) {
		return value == null ? 0 : value;
	}

	static double round(double value) {
		return Math.round(value * 100) / 100.0;
	}
}
