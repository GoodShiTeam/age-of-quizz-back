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
import goodshi.ageofquizz.repository.QuestionRepository;
import jakarta.transaction.Transactional;

@Service
public class QuestionService {

	@Autowired
	private QuestionRepository questionRepository;

	@Transactional
	public Question createQuestion(QuestionCreateRequestDTO request, User author) {

		Question question = new Question(request.getTheme(), request.getLibelle(), request.getType(), author);
		question.setFileUrl(request.getFileUrl());

		for (AnswerCreateRequestDTO answerRequest : request.getAnswers()) {
			Answer answer = new Answer(answerRequest.getValue(), answerRequest.isCorrect());
			question.addAnswer(answer);
		}

		return questionRepository.save(question);
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
}
