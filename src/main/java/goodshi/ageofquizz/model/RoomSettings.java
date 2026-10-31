package goodshi.ageofquizz.model;

import java.util.List;

import goodshi.ageofquizz.dto.FilterQuestionDTO;
import goodshi.ageofquizz.entity.Question.QuestionBuilding;
import goodshi.ageofquizz.entity.Question.QuestionCivilisation;
import goodshi.ageofquizz.entity.Question.QuestionTheme;

/**
 * Options de partie choisies par l'hôte dans le lobby. Sert aussi de payload
 * pour PUT /multiplayer/rooms/{code}/settings (participantId pour un hôte
 * anonyme).
 */
public class RoomSettings {

	public static final int MIN_QUESTIONS = 1;
	public static final int MAX_QUESTIONS = 50;
	public static final int MIN_DURATION = 5;
	public static final int MAX_DURATION = 120;

	private int numberOfQuestions = 5;
	/** Durée d'une question, en secondes. */
	private int questionDuration = 15;
	private QuestionTheme theme;
	private QuestionCivilisation civilisation;
	private QuestionBuilding building;
	private List<QuestionTheme> excludedThemes;

	private String participantId;

	public RoomSettings() {
	}

	/** Ramène les valeurs numériques dans les bornes autorisées. */
	public void clamp() {
		numberOfQuestions = Math.max(MIN_QUESTIONS, Math.min(MAX_QUESTIONS, numberOfQuestions));
		questionDuration = Math.max(MIN_DURATION, Math.min(MAX_DURATION, questionDuration));
		// Un thème à la fois choisi et exclu n'a pas de sens : l'exclusion l'emporte.
		if (theme != null && excludedThemes != null && excludedThemes.contains(theme)) {
			theme = null;
		}
	}

	public FilterQuestionDTO toFilter() {
		FilterQuestionDTO filter = new FilterQuestionDTO();
		filter.setNumberOfQuestions(numberOfQuestions);
		filter.setTheme(theme);
		filter.setCivilisation(civilisation);
		filter.setBuilding(building);
		filter.setExcludedThemes(excludedThemes);
		return filter;
	}

	public int getNumberOfQuestions() {
		return numberOfQuestions;
	}

	public void setNumberOfQuestions(int numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}

	public int getQuestionDuration() {
		return questionDuration;
	}

	public void setQuestionDuration(int questionDuration) {
		this.questionDuration = questionDuration;
	}

	public QuestionTheme getTheme() {
		return theme;
	}

	public void setTheme(QuestionTheme theme) {
		this.theme = theme;
	}

	public QuestionCivilisation getCivilisation() {
		return civilisation;
	}

	public void setCivilisation(QuestionCivilisation civilisation) {
		this.civilisation = civilisation;
	}

	public QuestionBuilding getBuilding() {
		return building;
	}

	public void setBuilding(QuestionBuilding building) {
		this.building = building;
	}

	public List<QuestionTheme> getExcludedThemes() {
		return excludedThemes;
	}

	public void setExcludedThemes(List<QuestionTheme> excludedThemes) {
		this.excludedThemes = excludedThemes;
	}

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

}
