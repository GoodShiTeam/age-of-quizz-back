package goodshi.ageofquizz.dto;

import goodshi.ageofquizz.entity.Question.QuestionBuilding;
import goodshi.ageofquizz.entity.Question.QuestionCivilisation;
import goodshi.ageofquizz.entity.Question.QuestionTheme;

public class FilterQuestionDTO {

	private QuestionBuilding building;
	private QuestionCivilisation civilisation;
	private QuestionTheme theme;
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

	public int getNumberOfQuestions() {
		return numberOfQuestions;
	}

	public void setNumberOfQuestions(int numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}

}
