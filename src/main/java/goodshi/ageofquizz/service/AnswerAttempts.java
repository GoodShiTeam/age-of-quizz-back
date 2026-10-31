package goodshi.ageofquizz.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * user_answer contient une ligne par réponse cochée, sans identifiant de
 * tentative : les lignes d'une même question enregistrées à moins de
 * {@link #SAME_ATTEMPT_WINDOW} les unes des autres forment une seule tentative.
 */
final class AnswerAttempts {

	private static final Duration SAME_ATTEMPT_WINDOW = Duration.ofSeconds(5);

	/** Identifiant de la réponse fictive enregistrée pour une question sans réponse. */
	private static final int NO_ANSWER_ID = 0;

	private AnswerAttempts() {
	}

	/** Une ligne de user_answer, réduite à ce qui sert aux stats. */
	record AnswerRow(Integer questionId, Integer answerId, LocalDateTime answeredAt, BigDecimal responseTime) {
	}

	/** Une question répondue une fois. */
	record Attempt(Integer questionId, boolean correct, double responseTimeSeconds, LocalDateTime answeredAt) {
	}

	/** Paires [questionId, answerId] des bonnes réponses -> map questionId -> ids. */
	static Map<Integer, Set<Integer>> correctIdsByQuestion(List<Object[]> pairs) {
		Map<Integer, Set<Integer>> result = new HashMap<>();
		for (Object[] row : pairs) {
			result.computeIfAbsent((Integer) row[0], k -> new HashSet<>()).add((Integer) row[1]);
		}
		return result;
	}

	/** Regroupe les réponses d'UN joueur en tentatives. */
	static List<Attempt> group(List<AnswerRow> rows, Map<Integer, Set<Integer>> correctIdsByQuestion) {
		if (rows.isEmpty()) {
			return List.of();
		}
		List<AnswerRow> sorted = new ArrayList<>(rows);
		sorted.sort(Comparator.comparing(AnswerRow::questionId).thenComparing(AnswerAttempts::answeredAt));

		List<Attempt> attempts = new ArrayList<>();
		List<AnswerRow> group = new ArrayList<>();
		for (AnswerRow row : sorted) {
			if (!group.isEmpty() && !sameAttempt(group.get(group.size() - 1), row)) {
				attempts.add(toAttempt(group, correctIdsByQuestion));
				group = new ArrayList<>();
			}
			group.add(row);
		}
		attempts.add(toAttempt(group, correctIdsByQuestion));
		return attempts;
	}

	private static boolean sameAttempt(AnswerRow previous, AnswerRow next) {
		return previous.questionId().equals(next.questionId())
				&& Duration.between(answeredAt(previous), answeredAt(next)).compareTo(SAME_ATTEMPT_WINDOW) <= 0;
	}

	private static Attempt toAttempt(List<AnswerRow> group, Map<Integer, Set<Integer>> correctIdsByQuestion) {
		AnswerRow first = group.get(0);
		Set<Integer> selected = group.stream().map(AnswerRow::answerId).filter(id -> id != NO_ANSWER_ID)
				.collect(Collectors.toSet());
		Set<Integer> correct = correctIdsByQuestion.getOrDefault(first.questionId(), Set.of());
		return new Attempt(first.questionId(), !selected.isEmpty() && selected.equals(correct),
				first.responseTime() == null ? 0 : first.responseTime().doubleValue(), first.answeredAt());
	}

	private static LocalDateTime answeredAt(AnswerRow row) {
		return row.answeredAt() == null ? LocalDateTime.MIN : row.answeredAt();
	}
}
