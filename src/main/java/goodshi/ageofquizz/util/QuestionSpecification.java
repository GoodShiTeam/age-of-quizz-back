package goodshi.ageofquizz.util;

import org.springframework.data.jpa.domain.Specification;

import goodshi.ageofquizz.entity.Question;

public class QuestionSpecification {

	public static Specification<Question> hasTheme(Question.QuestionTheme theme) {
		return (root, query, cb) -> theme == null ? cb.conjunction() : cb.equal(root.get("theme"), theme);
	}

	public static Specification<Question> hasCivilisation(Question.QuestionCivilisation civilisation) {
		return (root, query, cb) -> civilisation == null ? cb.conjunction()
				: cb.equal(root.get("civilisation"), civilisation);
	}

	public static Specification<Question> hasBuilding(Question.QuestionBuilding building) {
		return (root, query, cb) -> building == null ? cb.conjunction() : cb.equal(root.get("building"), building);
	}
}
