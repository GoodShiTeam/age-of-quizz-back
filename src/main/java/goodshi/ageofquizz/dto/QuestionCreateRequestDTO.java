package goodshi.ageofquizz.dto;

import java.util.List;

import goodshi.ageofquizz.entity.Question.QuestionTheme;
import goodshi.ageofquizz.entity.Question.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class QuestionCreateRequestDTO {

	@NotNull
	private QuestionTheme theme;

	@NotBlank
	private String libelle;

	private String fileUrl;

	@NotNull
	private QuestionType type;

	@NotEmpty
	private List<AnswerCreateRequestDTO> answers;

	public QuestionTheme getTheme() {
		return theme;
	}

	public void setTheme(QuestionTheme theme) {
		this.theme = theme;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public QuestionType getType() {
		return type;
	}

	public void setType(QuestionType type) {
		this.type = type;
	}

	public List<AnswerCreateRequestDTO> getAnswers() {
		return answers;
	}

	public void setAnswers(List<AnswerCreateRequestDTO> answers) {
		this.answers = answers;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
}
