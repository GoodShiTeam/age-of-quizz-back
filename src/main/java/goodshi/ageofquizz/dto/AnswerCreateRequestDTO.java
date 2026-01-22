package goodshi.ageofquizz.dto;

import jakarta.validation.constraints.NotBlank;

public class AnswerCreateRequestDTO {

	@NotBlank
	private String value;

	private boolean isCorrect;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean correct) {
		isCorrect = correct;
	}
}
