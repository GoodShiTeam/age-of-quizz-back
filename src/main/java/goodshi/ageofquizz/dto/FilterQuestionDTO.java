package goodshi.ageofquizz.dto;

import java.util.List;

import goodshi.ageofquizz.entity.Question.QuestionBuilding;
import goodshi.ageofquizz.entity.Question.QuestionCivilisation;
import goodshi.ageofquizz.entity.Question.QuestionTheme;

public class FilterQuestionDTO {

	private QuestionBuilding building;
	private QuestionCivilisation civilisation;
	private QuestionTheme theme;
	/** Thèmes à écarter, y compris des questions aléatoires de complément. */
	private List<QuestionTheme> excludedThemes;
	private int numberOfQuestions;

	public QuestionBuilding getBuilding() {
		return building;
	}

	public void setBuilding(QuestionBuilding building) {
		this.building = building;
	}

	public QuestionCivilisation getCivilisation() {
		return civilisation;
	}

	public void setCivilisation(QuestionCivilisation civilisation) {
		this.civilisation = civilisation;
	}

	public QuestionTheme getTheme() {
		return theme;
	}

	public void setTheme(QuestionTheme theme) {
		this.theme = theme;
	}

	public List<QuestionTheme> getExcludedThemes() {
		return excludedThemes;
	}

	public void setExcludedThemes(List<QuestionTheme> excludedThemes) {
		this.excludedThemes = excludedThemes;
	}

	public int getNumberOfQuestions() {
		return numberOfQuestions;
	}

	public void setNumberOfQuestions(int numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}

}
