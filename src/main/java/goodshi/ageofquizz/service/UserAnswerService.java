package goodshi.ageofquizz.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goodshi.ageofquizz.dto.UserAnswerBatchRequest;
import goodshi.ageofquizz.dto.UserAnswerRequest;
import goodshi.ageofquizz.entity.Answer;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.entity.UserAnswer;
import goodshi.ageofquizz.repository.AnswerRepository;
import goodshi.ageofquizz.repository.QuestionRepository;
import goodshi.ageofquizz.repository.UserAnswerRepository;
import goodshi.ageofquizz.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

//UserAnswerService.java
@Service
public class UserAnswerService {

	@Autowired
	private UserAnswerRepository userAnswerRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Transactional
	public void submitAnswer(Long userId, UserAnswerRequest dto) {

		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

		Question question = questionRepository.findById(dto.getQuestionId())
				.orElseThrow(() -> new EntityNotFoundException("Question not found"));

		List<Answer> answers = answerRepository.findAllById(dto.getAnswerIds());

		if (answers.size() != dto.getAnswerIds().size()) {
			throw new IllegalArgumentException("One or more answers not found");
		}
		if (answers.size() == 0) {
			createEmptyUserAnswer(user, question);
		}

		for (Answer answer : answers) {

			if (!answer.getQuestion().getId().equals(question.getId())) {
				throw new IllegalArgumentException("Answer does not belong to question");
			}

			createUserAnswer(dto, user, question, answer);
		}
	}

	private void createUserAnswer(UserAnswerRequest dto, User user, Question question, Answer answer) {
		UserAnswer ua = new UserAnswer();
		ua.setUser(user);
		ua.setQuestion(question);
		ua.setAnswer(answer);
		ua.setResponseTimeSeconds(dto.getResponseTimeSeconds());

		userAnswerRepository.save(ua);
	}

	private void createEmptyUserAnswer(User user, Question question) {
		UserAnswer ua = new UserAnswer();
		ua.setUser(user);
		ua.setQuestion(question);
		ua.setAnswer(answerRepository.findById(0).get());
		ua.setResponseTimeSeconds(new BigDecimal(0));

		userAnswerRepository.save(ua);
	}

	@Transactional
	public void submitAnswers(Long userId, UserAnswerBatchRequest userAnswerBatchRequest) {
		for (UserAnswerRequest dto : userAnswerBatchRequest.getUserAnswerRequests()) {
			submitAnswer(userId, dto);
		}
	}
}