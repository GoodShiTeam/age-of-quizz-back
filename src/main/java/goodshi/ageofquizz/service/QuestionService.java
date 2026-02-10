package goodshi.ageofquizz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.dto.AnswerCreateRequestDTO;
import goodshi.ageofquizz.dto.QuestionCreateRequestDTO;
import goodshi.ageofquizz.entity.Answer;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.Question.QuestionStatus;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.repository.QuestionRepository;
import jakarta.transaction.Transactional;

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

		for (AnswerCreateRequestDTO answerRequest : request.getAnswers()) {
			Answer answer = new Answer(answerRequest.getValue(), answerRequest.isCorrect());
			question.addAnswer(answer);
		}
		return question;
	}

	public List<Question> getAllQuestions() {
		return questionRepository.findAll();
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
}
