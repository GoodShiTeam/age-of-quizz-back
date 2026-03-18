package goodshi.ageofquizz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.dto.FilterQuestionDTO;
import goodshi.ageofquizz.dto.QuestionCreateRequestDTO;
import goodshi.ageofquizz.dto.QuestionDTO;
import goodshi.ageofquizz.dto.UserAnswerBatchRequest;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.service.CustomUserDetailsService;
import goodshi.ageofquizz.service.QuestionService;
import goodshi.ageofquizz.service.UserAnswerService;
import goodshi.ageofquizz.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/questions")
public class QuestionController {

	@Autowired
	private QuestionService questionService;

	@Autowired
	private UserAnswerService userAnswerService;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private UserService userService;

	@PostMapping
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<Integer> createQuestion(@Valid @RequestBody QuestionCreateRequestDTO request) {
		Question savedQuestion = questionService.createQuestion(request, getAuthenticatedUser());
		return ResponseEntity.status(HttpStatus.CREATED).body(savedQuestion.getId());
	}

	@PutMapping
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<Integer> updateQuestion(@Valid @RequestBody QuestionCreateRequestDTO request) {
		Question updatedQuestion = questionService.updateQuestion(request, getAuthenticatedUser());
		return ResponseEntity.status(HttpStatus.OK).body(updatedQuestion.getId());
	}

	private User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			return userService.getUser(0L);
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof UserDetails userDetails) {
			return customUserDetailsService.findByUsername(userDetails.getUsername());
		}

		return userService.getUser(0L);
	}

	@GetMapping("/all")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<List<QuestionDTO>> getAllQuestions() {

		List<QuestionDTO> dtos = questionService.getAllQuestions();

		return ResponseEntity.ok(dtos);
	}

	@PostMapping("/quizz")
	public ResponseEntity<List<QuestionDTO>> getQuestions(@Valid @RequestBody FilterQuestionDTO filterQuestionDTO) {

		List<QuestionDTO> dtos = questionService.getQuestions(filterQuestionDTO);

		return ResponseEntity.ok(dtos);
	}

	@PutMapping("/{id}/{status}")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<QuestionDTO> updateStatusQuestion(@PathVariable("id") Integer id,
			@PathVariable("status") Question.QuestionStatus status) {
		Question updated = questionService.updateStatusQuestion(id, status);
		return ResponseEntity.ok(QuestionDTO.fromEntity(updated));
	}

	@PostMapping("/submit-answers")
	public ResponseEntity<Void> submitAnswers(@RequestBody @Valid UserAnswerBatchRequest request) {
		userAnswerService.submitAnswers(getAuthenticatedUser().getId(), request);
		return ResponseEntity.ok().build();
	}

}
