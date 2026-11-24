package goodshi.ageofquizz.event.advent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goodshi.ageofquizz.entity.AdventAnswer;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.enums.RoleEnum;
import goodshi.ageofquizz.event.advent.AdventDtos.CalendarDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.DayDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.DaySummaryDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.JokersDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.LeaderboardEntryDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.ResultDTO;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.Question.QuestionStatus;
import goodshi.ageofquizz.event.advent.AdventQuestion.Quiz;
import goodshi.ageofquizz.repository.QuestionRepository;
import goodshi.ageofquizz.service.QuizFailureService;
import goodshi.ageofquizz.repository.AdventAnswerRepository;
import goodshi.ageofquizz.repository.UserAnswerRepository;
import goodshi.ageofquizz.service.PlayerNames;

/**
 * Calendrier de l'avent : la case N se débloque le N-ième jour à partir de
 * {@code app.event.advent.start}, et reste ouvrable (rattrapage) jusqu'à la
 * fin de l'event. Un essai par case et par joueur connecté (plusieurs pour
 * le format ZOOM, dont l'image se dézoome à chaque erreur).
 *
 * Jokers : chaque tranche de {@link #QUESTIONS_PER_JOKER} questions de quiz
 * (solo ou multi) jouées pendant l'event donne un joker, qui permet de
 * rejouer une case ratée. Après une erreur, la bonne réponse reste cachée tant
 * que le joueur ne la révèle pas ; la révéler ferme la case aux jokers.
 *
 * Mode test : un admin ouvre toutes les cases, même hors période, et
 * n'apparaît pas au classement ; il peut effacer ses réponses.
 */
@Service
public class AdventService {

	public static final int QUESTIONS_PER_JOKER = 10;

	private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
	private static final String ALREADY_ANSWERED = "Tu as déjà répondu à cette case.";

	private final AdventAnswerRepository answerRepository;
	private final UserAnswerRepository userAnswerRepository;
	private final QuestionRepository questionRepository;
	private final QuizFailureService quizFailureService;
	private final LocalDate startDate;
	private final LocalDate endDate;

	public AdventService(AdventAnswerRepository answerRepository, UserAnswerRepository userAnswerRepository,
			QuestionRepository questionRepository, QuizFailureService quizFailureService,
			@Value("${app.event.advent.start:2026-12-01}") LocalDate startDate,
			@Value("${app.event.advent.end:2027-01-01}") LocalDate endDate) {
		this.answerRepository = answerRepository;
		this.userAnswerRepository = userAnswerRepository;
		this.questionRepository = questionRepository;
		this.quizFailureService = quizFailureService;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/** Erreur métier traduite en statut HTTP par le contrôleur. */
	public static class AdventException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final HttpStatus status;

		AdventException(HttpStatus status, String message) {
			super(message);
			this.status = status;
		}

		public HttpStatus getStatus() {
			return status;
		}
	}

	/* ======================= Période ======================= */

	LocalDate today() {
		return LocalDate.now(ZONE);
	}

	private int year() {
		return startDate.getYear();
	}

	public boolean isActive() {
		LocalDate today = today();
		return !today.isBefore(startDate) && !today.isAfter(endDate);
	}

	private int unlockedDays() {
		if (!isActive()) {
			return 0;
		}
		long elapsed = ChronoUnit.DAYS.between(startDate, today()) + 1;
		return (int) Math.min(AdventQuestions.DAYS, elapsed);
	}

	/* ======================= Lecture ======================= */

	/** Admin : mode test (toutes les cases, hors classement). */
	static boolean isAdmin(User user) {
		return user != null && user.getRoles() != null
				&& user.getRoles().stream().anyMatch(role -> role.getName() == RoleEnum.ADMIN);
	}

	/** @param user utilisateur connecté, ou null */
	public CalendarDTO getCalendar(User user) {
		boolean preview = isAdmin(user);
		int unlocked = preview ? AdventQuestions.DAYS : unlockedDays();
		Map<Integer, AdventAnswer> answers = user == null ? Map.of() : answersByDay(user);

		List<DaySummaryDTO> days = new ArrayList<>();
		for (int day = 1; day <= AdventQuestions.DAYS; day++) {
			AdventAnswer answer = answers.get(day);
			int maxAttempts = AdventQuestions.forDay(day).map(AdventQuestion::maxAttempts).orElse(1);
			days.add(new DaySummaryDTO(day, day <= unlocked, AdventQuestions.availableDays().contains(day),
					answer != null, answer == null ? null : answer.isCorrect(),
					answer == null ? 0 : answer.getJokersUsed(),
					answer != null && attemptsLeft(maxAttempts, answer) > 0));
		}

		if (user == null) {
			return new CalendarDTO(isActive(), startDate, endDate, today(), AdventQuestions.DAYS, unlocked, days,
					null, null, null, false);
		}
		int score = (int) answers.values().stream().filter(AdventAnswer::isCorrect).count();
		return new CalendarDTO(isActive() || preview, startDate, endDate, today(), AdventQuestions.DAYS, unlocked,
				days, score, answers.size(), jokers(user), preview);
	}

	public DayDTO getDay(int day, User user) {
		AdventQuestion template = openableQuestion(day, user);
		Optional<AdventAnswer> answer = answerRepository.findByUserAndYearAndDay(user, year(), day);
		AdventQuestion question = forPlayer(template, user, answer.orElse(null));
		ResultDTO result = answer.map(a -> toResult(question, a, jokers(user).available())).orElse(null);
		Quiz quiz = question.quiz();
		String mediaUrl = question.format() == AdventQuestion.Format.ZOOM
				? question.zoomImage(answer.map(AdventAnswer::getAttempts).orElse(0),
						answer.map(a -> attemptsLeft(question.maxAttempts(), a) == 0).orElse(false))
				: question.mediaUrl();
		return new DayDTO(day, question.format(), question.prompt(), mediaUrl, question.choices(),
				question.choiceImages(), question.targets(), question.targetImages(),
				quiz == null ? null : quiz.title(), quiz == null ? null : quiz.shownIntro(),
				quiz == null ? null : quiz.mediaType(), quiz == null ? null : quiz.fileUrl(),
				question.format() == AdventQuestion.Format.SELECT || (quiz != null && quiz.multiple()),
				question.numbers() == null ? null : question.numbers().tolerance(),
				question.numbers() == null ? null : question.numbers().minCorrect(),
				question.grid() == null ? null : question.grid().image(),
				question.grid() == null ? null : question.grid().columns(), question.maxAttempts(), result);
	}

	/* ======================= Réponse ======================= */

	/**
	 * Premier essai, ou nouvel essai payé par un joker ({@code useJoker}) sur
	 * une case ratée et pas encore révélée.
	 */
	@Transactional
	public ResultDTO answer(int day, String answer, boolean useJoker, User user) {
		AdventQuestion template = openableQuestion(day, user);
		AdventAnswer existing = lockAnswers(user).get(day);
		AdventQuestion question = forPlayer(template, user, existing);
		if (!question.isValidAnswer(answer)) {
			throw new AdventException(HttpStatus.BAD_REQUEST, "Réponse invalide.");
		}
		boolean correct = question.isCorrect(answer.trim());
		String stored = question.displayAnswer(answer.trim());
		if (stored.length() > 500) {
			stored = stored.substring(0, 500);
		}
		String trimmed = stored;

		if (existing == null) {
			AdventAnswer saved;
			Integer quizQuestionId = question.quiz() == null ? null : question.quiz().questionId();
			try {
				saved = answerRepository
						.saveAndFlush(new AdventAnswer(user, year(), day, trimmed, correct, quizQuestionId));
			} catch (DataIntegrityViolationException e) {
				// Double clic : la contrainte unique a tranché.
				throw new AdventException(HttpStatus.CONFLICT, ALREADY_ANSWERED);
			}
			return toResult(question, saved, jokers(user).available());
		}

		// Format ZOOM : essais gratuits avant de devoir passer par un joker
		if (attemptsLeft(question.maxAttempts(), existing) > 0) {
			existing.retryFree(trimmed, correct);
			answerRepository.saveAndFlush(existing);
			return toResult(question, existing, jokers(user).available());
		}
		if (!useJoker) {
			throw new AdventException(HttpStatus.CONFLICT, ALREADY_ANSWERED);
		}
		if (existing.isCorrect()) {
			throw new AdventException(HttpStatus.CONFLICT, "Tu as déjà trouvé cette case !");
		}
		if (existing.isRevealed()) {
			throw new AdventException(HttpStatus.CONFLICT,
					"Tu as déjà vu la réponse de cette case : elle ne peut plus être rejouée.");
		}
		if (jokers(user).available() <= 0) {
			throw new AdventException(HttpStatus.CONFLICT, "Tu n'as plus de joker.");
		}
		existing.retryWithJoker(trimmed, correct);
		answerRepository.saveAndFlush(existing);
		return toResult(question, existing, jokers(user).available());
	}

	/** Affiche la bonne réponse d'une case ratée ; elle ne pourra plus être rejouée. */
	@Transactional
	public ResultDTO reveal(int day, User user) {
		AdventQuestion template = openableQuestion(day, user);
		AdventAnswer existing = lockAnswers(user).get(day);
		if (existing == null) {
			throw new AdventException(HttpStatus.CONFLICT, "Réponds d'abord à cette case.");
		}
		AdventQuestion question = forPlayer(template, user, existing);
		existing.reveal();
		answerRepository.saveAndFlush(existing);
		return toResult(question, existing, jokers(user).available());
	}

	/** Mode test : efface les réponses de l'admin pour rejouer toutes les cases. */
	@Transactional
	public void resetPreview(User user) {
		if (!isAdmin(user)) {
			throw new AdventException(HttpStatus.FORBIDDEN, "Réservé aux admins.");
		}
		answerRepository.deleteByUserAndYear(user, year());
	}

	/* ======================= Case « rattrapage » ======================= */

	/**
	 * La question à poser à ce joueur : la question écrite dans
	 * AdventQuestions, ou pour une case rattrapage une question du quiz qu'il a
	 * ratée avant l'ouverture de la case (sinon une question au hasard). Le
	 * tirage est stable : mêmes données -> même question, puis la question
	 * retenue est mémorisée avec la réponse.
	 */
	private AdventQuestion forPlayer(AdventQuestion template, User user, AdventAnswer existing) {
		if (template.format() != AdventQuestion.Format.QUIZ_RETRY) {
			return template;
		}
		int day = template.day();
		List<Integer> failed = quizFailureService.failedQuestionIds(user.getId(),
				startDate.plusDays(day - 1L).atStartOfDay());
		long seed = Objects.hash(user.getId(), year(), day);

		Integer questionId = existing == null ? null : existing.getQuizQuestionId();
		if (questionId == null) {
			List<Integer> pool = failed.isEmpty() ? questionRepository.findIdsByStatus(QuestionStatus.VALIDATED)
					: failed;
			if (pool.isEmpty()) {
				throw new AdventException(HttpStatus.NOT_FOUND, "Aucune question de quiz disponible.");
			}
			questionId = pool.get((int) Math.floorMod(seed, (long) pool.size()));
		}

		Question question = questionRepository.findByIdsWithAnswers(List.of(questionId)).stream().findFirst()
				.orElseThrow(() -> new AdventException(HttpStatus.NOT_FOUND,
						"La question de quiz de cette case n'existe plus."));
		List<Map.Entry<String, Boolean>> answers = AdventQuestion.shuffled(
				question.getAnswers().stream()
						.map(a -> Map.entry(a.getValue(), Boolean.TRUE.equals(a.isCorrect()))).toList(),
				seed * 31 + questionId);
		return template.toQuiz(questionId, failed.contains(questionId), question.getLibelle(),
				question.getExplication(), question.getType() == null ? null : question.getType().name(),
				question.getFileUrl(), answers);
	}

	/* ======================= Jokers ======================= */

	private JokersDTO jokers(User user) {
		long played = userAnswerRepository.countByUserBetween(user.getId(), startDate.atStartOfDay(),
				endDate.plusDays(1).atStartOfDay());
		int earned = (int) (played / QUESTIONS_PER_JOKER);
		int used = (int) answerRepository.sumJokersUsed(user, year());
		int nextIn = QUESTIONS_PER_JOKER - (int) (played % QUESTIONS_PER_JOKER);
		return new JokersDTO(Math.max(0, earned - used), earned, used, played, QUESTIONS_PER_JOKER, nextIn);
	}

	/* ======================= Classement ======================= */

	/**
	 * Classé par bonnes réponses (avec ou sans joker), puis par celui qui a
	 * atteint ce score le premier, puis par cases ouvertes.
	 */
	public List<LeaderboardEntryDTO> getLeaderboard(User me) {
		record Row(User user, int score, int answered, Instant lastCorrectAt, int jokersUsed) {
		}
		List<Row> rows = answerRepository.scoresByUser(year()).stream()
				// Les admins testent l'event : hors classement
				.filter(r -> !isAdmin((User) r[0]))
				.map(r -> new Row((User) r[0], ((Number) r[1]).intValue(), ((Number) r[2]).intValue(),
						(Instant) r[3], r[4] == null ? 0 : ((Number) r[4]).intValue()))
				.sorted(Comparator.comparingInt(Row::score).reversed()
						.thenComparing(Row::lastCorrectAt, Comparator.nullsLast(Comparator.naturalOrder()))
						.thenComparing(Comparator.comparingInt(Row::answered).reversed()))
				.toList();

		List<LeaderboardEntryDTO> result = new ArrayList<>();
		int rank = 0;
		Row previous = null;
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			// Ex aequo : même score atteint au même instant (rare) -> même rang.
			if (previous == null || previous.score() != row.score()
					|| !Objects.equals(previous.lastCorrectAt(), row.lastCorrectAt())) {
				rank = i + 1;
			}
			previous = row;
			User user = row.user();
			Long profileId = user.getUserProfile() == null ? null : user.getUserProfile().getId();
			result.add(new LeaderboardEntryDTO(rank, user.getId(), profileId, PlayerNames.displayName(user),
					row.score(), row.answered(), row.jokersUsed(), me != null && Objects.equals(me.getId(), user.getId())));
		}
		return result;
	}

	/* ======================= Outils ======================= */

	private AdventQuestion openableQuestion(int day, User user) {
		boolean preview = isAdmin(user);
		if (!isActive() && !preview) {
			throw new AdventException(HttpStatus.FORBIDDEN, "Le calendrier de l'avent n'est pas ouvert.");
		}
		if (day < 1 || day > (preview ? AdventQuestions.DAYS : unlockedDays())) {
			throw new AdventException(HttpStatus.FORBIDDEN, "Pas de triche : cette case n'est pas encore ouverte !");
		}
		return AdventQuestions.forDay(day)
				.orElseThrow(() -> new AdventException(HttpStatus.NOT_FOUND, "Cette case est encore vide."));
	}

	private Map<Integer, AdventAnswer> answersByDay(User user) {
		return byDay(answerRepository.findByUserAndYear(user, year()));
	}

	private Map<Integer, AdventAnswer> lockAnswers(User user) {
		return byDay(answerRepository.lockByUserAndYear(user, year()));
	}

	private static Map<Integer, AdventAnswer> byDay(List<AdventAnswer> answers) {
		return answers.stream().collect(Collectors.toMap(AdventAnswer::getDay, Function.identity(), (a, b) -> a));
	}

	/** Essais gratuits restants : 0 une fois la case trouvée ou révélée. */
	private static int attemptsLeft(int maxAttempts, AdventAnswer answer) {
		return answer.isRevealed() ? 0 : Math.max(0, maxAttempts - answer.getAttempts());
	}

	private static ResultDTO toResult(AdventQuestion question, AdventAnswer answer, int jokersAvailable) {
		boolean revealed = answer.isRevealed();
		int attemptsLeft = attemptsLeft(question.maxAttempts(), answer);
		return new ResultDTO(answer.getAnswer(), answer.isCorrect(), revealed,
				revealed ? question.correctAnswer() : null, revealed ? question.explanation() : null,
				answer.getJokersUsed(), !revealed && attemptsLeft == 0 && jokersAvailable > 0,
				answer.getAttempts(), attemptsLeft, question.zoomImage(answer.getAttempts(), attemptsLeft == 0));
	}
}
