package goodshi.ageofquizz.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.dto.AnswerCreateRequestDTO;
import goodshi.ageofquizz.dto.FilterQuestionDTO;
import goodshi.ageofquizz.dto.QuestionCreateRequestDTO;
import goodshi.ageofquizz.dto.QuestionDTO;
import goodshi.ageofquizz.entity.Answer;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.Question.QuestionStatus;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.repository.QuestionRepository;
import goodshi.ageofquizz.util.QuestionSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class QuestionService {

	@Autowired
	private QuestionRepository questionRepository;

	@Transactional
	public Question createQuestion(QuestionCreateRequestDTO request, User author) {

		Question question = new Question(request.getTheme(), request.getLibelle(), request.getType(), author);

		Question createdQuestion = buildQuestion(question, request, author);

		return questionRepository.save(createdQuestion);
	}

	private Question buildQuestion(Question question, QuestionCreateRequestDTO request, User author) {
		question.setFileUrl(request.getFileUrl());
		question.setCivilisation(request.getCivilisation());
		question.setBuilding(request.getBuilding());
		question.setExplication(request.getExplication());

		for (AnswerCreateRequestDTO answerRequest : request.getAnswers()) {
			Answer answer = new Answer(answerRequest.getValue(), answerRequest.isCorrect());
			question.addAnswer(answer);
		}
		return question;
	}

	public List<QuestionDTO> getAllQuestions() {
		return questionRepository.findAll().stream().map(QuestionDTO::fromEntity).toList();
	}

	public Question updateStatusQuestion(Integer id, QuestionStatus status) {
		return questionRepository.findById(id).map(question -> {
			question.setStatus(status);
			return questionRepository.save(question);
		}).orElseThrow(() -> new RuntimeException("Question id : " + id + "doesn't exist"));
	}

	public Question updateQuestion(QuestionCreateRequestDTO request, User author) {
		Question question = questionRepository.findById(request.getId()).orElseThrow(
				() -> new ResourceNotFoundException("Question with id : " + request.getId() + " doesn't exist"));

		question.getAnswers().clear();

		Question updatedQuestion = buildQuestion(question, request, author);
		updatedQuestion.setTheme(request.getTheme());
		updatedQuestion.setLibelle(request.getLibelle());
		updatedQuestion.setType(request.getType());
		updatedQuestion.setStatus(QuestionStatus.CREATED_REVIEW);

		return questionRepository.save(updatedQuestion);
	}

	public List<QuestionDTO> getQuestions(@Valid FilterQuestionDTO filter) {

		int wanted = filter.getNumberOfQuestions();

		Specification<Question> spec = Specification.where(QuestionSpecification.hasTheme(filter.getTheme()))
				.and(QuestionSpecification.hasCivilisation(filter.getCivilisation()))
				.and(QuestionSpecification.isStatus(QuestionStatus.VALIDATED))
				.and(QuestionSpecification.hasBuilding(filter.getBuilding()));

		List<Question> filteredQuestions = questionRepository.findAll(spec);

		Collections.shuffle(filteredQuestions);

		List<Question> result = new ArrayList<>(filteredQuestions.stream().limit(wanted).toList());

		int missing = wanted - result.size();

		if (missing > 0) {
			List<Integer> excludedIds = result.stream().map(Question::getId).toList();

			List<Question> randomQuestions = questionRepository.findRandomQuestions(QuestionStatus.VALIDATED,
					excludedIds.isEmpty() ? null : excludedIds, PageRequest.of(0, missing));

			result.addAll(randomQuestions);
		}

		return result.stream().map(QuestionDTO::fromEntity).toList();
	}
}
