package goodshi.ageofquizz.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class UserAnswerRequest {

	@NotNull
	private Integer questionId;

	private List<Integer> answerIds;

	@NotNull
	@DecimalMin(value = "0.0", inclusive = false)
	private BigDecimal responseTimeSeconds;

	public Integer getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	public List<Integer> getAnswerIds() {
		return answerIds;
	}

	public void setAnswerIds(List<Integer> answerIds) {
		this.answerIds = answerIds;
	}

	public BigDecimal getResponseTimeSeconds() {
		return responseTimeSeconds;
	}

	public void setResponseTimeSeconds(BigDecimal responseTimeSeconds) {
		this.responseTimeSeconds = responseTimeSeconds;
	}

}