package goodshi.ageofquizz.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class UserAnswerBatchRequest {

	@NotEmpty
	@Valid
	private List<UserAnswerRequest> userAnswerRequests;

	public List<UserAnswerRequest> getUserAnswerRequests() {
		return userAnswerRequests;
	}

	public void setUserAnswerRequests(List<UserAnswerRequest> userAnswerRequests) {
		this.userAnswerRequests = userAnswerRequests;
	}

}