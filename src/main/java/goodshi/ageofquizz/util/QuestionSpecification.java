package goodshi.ageofquizz.util;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import goodshi.ageofquizz.entity.Question;

public class QuestionSpecification {

	public static Specification<Question> hasTheme(Question.QuestionTheme theme) {
		return (root, query, cb) -> theme == null ? cb.conjunction() : cb.equal(root.get("theme"), theme);
	}

	/** Les questions sans thème ne sont pas écartées. */
	public static Specification<Question> hasNoneOfThemes(List<Question.QuestionTheme> themes) {
		return (root, query, cb) -> themes == null || themes.isEmpty() ? cb.conjunction()
				: cb.or(cb.isNull(root.get("theme")), cb.not(root.get("theme").in(themes)));
	}

	public static Specification<Question> hasCivilisation(Question.QuestionCivilisation civilisation) {
		return (root, query, cb) -> civilisation == null ? cb.conjunction()
				: cb.equal(root.get("civilisation"), civilisation);
	}

	public static Specification<Question> hasBuilding(Question.QuestionBuilding building) {
		return (root, query, cb) -> building == null ? cb.conjunction() : cb.equal(root.get("building"), building);
	}

	public static Specification<Question> isStatus(Question.QuestionStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
	}
}
