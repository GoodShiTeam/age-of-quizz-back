package goodshi.ageofquizz.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.Question.QuestionStatus;
import goodshi.ageofquizz.entity.UserAnswer;
import goodshi.ageofquizz.repository.AnswerRepository;
import goodshi.ageofquizz.repository.UserAnswerRepository;
import goodshi.ageofquizz.service.AnswerAttempts.AnswerRow;
import goodshi.ageofquizz.service.AnswerAttempts.Attempt;

/**
 * Questions de quiz ratées par un joueur (lecture seule), pour le
 * « rattrapage de Noël » du calendrier de l'avent.
 */
@Service
public class QuizFailureService {

	private final UserAnswerRepository userAnswerRepository;
	private final AnswerRepository answerRepository;

	public QuizFailureService(UserAnswerRepository userAnswerRepository, AnswerRepository answerRepository) {
		this.userAnswerRepository = userAnswerRepository;
		this.answerRepository = answerRepository;
	}

	/**
	 * Questions validées dont la dernière tentative du joueur avant
	 * {@code before} est ratée (une question réussie ensuite n'en fait pas
	 * partie), triées par id.
	 */
	@Transactional(readOnly = true)
	public List<Integer> failedQuestionIds(Long userId, LocalDateTime before) {
		Map<Integer, Question> questions = new HashMap<>();
		List<AnswerRow> rows = new ArrayList<>();
		for (UserAnswer ua : userAnswerRepository.findAllWithQuestionByUserId(userId)) {
			if (ua.getAnsweredAt() == null || !ua.getAnsweredAt().isBefore(before)) {
				continue;
			}
			questions.putIfAbsent(ua.getQuestion().getId(), ua.getQuestion());
			rows.add(new AnswerRow(ua.getQuestion().getId(), ua.getAnswer().getId(), ua.getAnsweredAt(),
					ua.getResponseTimeSeconds()));
		}
		if (rows.isEmpty()) {
			return List.of();
		}

		List<Attempt> attempts = AnswerAttempts.group(rows,
				AnswerAttempts.correctIdsByQuestion(answerRepository.findCorrectAnswerIds(questions.keySet())));
		Map<Integer, Attempt> latest = new HashMap<>();
		for (Attempt attempt : attempts) {
			latest.merge(attempt.questionId(), attempt,
					(a, b) -> Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder())
							.compare(a.answeredAt(), b.answeredAt()) >= 0 ? a : b);
		}
		return latest.values().stream().filter(a -> !a.correct())
				.filter(a -> questions.get(a.questionId()).getStatus() == QuestionStatus.VALIDATED)
				.map(Attempt::questionId).sorted().toList();
	}
}
