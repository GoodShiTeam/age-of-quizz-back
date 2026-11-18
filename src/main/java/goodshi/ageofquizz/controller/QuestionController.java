package goodshi.ageofquizz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import goodshi.ageofquizz.dto.QuestionReportDTO;
import goodshi.ageofquizz.dto.QuestionReportRequestDTO;
import goodshi.ageofquizz.dto.UserAnswerBatchRequest;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.service.AuthenticationFacade;
import goodshi.ageofquizz.service.QuestionReportService;
import goodshi.ageofquizz.service.QuestionService;
import goodshi.ageofquizz.service.UserAnswerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/questions")
public class QuestionController {

	@Autowired
	private QuestionService questionService;

	@Autowired
	private UserAnswerService userAnswerService;

	@Autowired
	private QuestionReportService questionReportService;

	@Autowired
	private AuthenticationFacade authenticationFacade;

	// Authentication resolved via AuthenticationFacade

	@PostMapping
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<Integer> createQuestion(@Valid @RequestBody QuestionCreateRequestDTO request) {
		var user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Question savedQuestion = questionService.createQuestion(request, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedQuestion.getId());
	}

	@PutMapping
	@PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
	public ResponseEntity<QuestionDTO> updateQuestion(@Valid @RequestBody QuestionCreateRequestDTO request) {
		var user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Question updatedQuestion = questionService.updateQuestion(request, user);
		// La question complète permet au front de mettre sa liste à jour sans tout recharger.
		return ResponseEntity.status(HttpStatus.OK).body(QuestionDTO.fromEntity(updatedQuestion));
	}

	@GetMapping("/all")
	@PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
	public ResponseEntity<List<QuestionDTO>> getAllQuestions() {

		List<QuestionDTO> dtos = questionService.getAllQuestions();

		return ResponseEntity.ok(dtos);
	}

	/** Auteurs proposés dans les filtres de partie (solo et multijoueur). */
	@GetMapping("/authors")
	public ResponseEntity<List<String>> getAuthors() {
		return ResponseEntity.ok(questionService.getAuthorsWithValidatedQuestions());
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
		var user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Question updated = questionService.updateStatusQuestion(id, status, user);
		return ResponseEntity.ok(QuestionDTO.fromEntity(updated));
	}

	/** Signalement d'une question par n'importe quel joueur connecté. */
	@PostMapping("/{id}/reports")
	public ResponseEntity<QuestionReportDTO> reportQuestion(@PathVariable("id") Integer id,
			@Valid @RequestBody QuestionReportRequestDTO request) {
		var user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(questionReportService.report(id, request, user));
	}

	/** Signalements non traités, affichés sur l'écran de révision. */
	@GetMapping("/reports")
	@PreAuthorize("hasAnyRole('AUTHOR', 'REVIEWER', 'ADMIN')")
	public ResponseEntity<List<QuestionReportDTO>> getOpenReports() {
		return ResponseEntity.ok(questionReportService.getOpenReports());
	}

	/** Le relecteur clôt tous les signalements ouverts d'une question. */
	@PutMapping("/{id}/reports/resolve")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<Void> resolveReports(@PathVariable("id") Integer id) {
		var user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		questionReportService.resolveAll(id, user);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/submit-answers")
	public ResponseEntity<Void> submitAnswers(@RequestBody @Valid UserAnswerBatchRequest request) {
		var user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		userAnswerService.submitAnswers(user.getId(), request);
		return ResponseEntity.ok().build();
	}

}
