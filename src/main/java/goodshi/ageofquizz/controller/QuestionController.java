package goodshi.ageofquizz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.dto.QuestionCreateRequestDTO;
import goodshi.ageofquizz.dto.QuestionDTO;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.service.CustomUserDetailsService;
import goodshi.ageofquizz.service.QuestionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/questions")
public class QuestionController {

	@Autowired
	private QuestionService questionService;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@PostMapping
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<Integer> createQuestion(@Valid @RequestBody QuestionCreateRequestDTO request) {
		Question savedQuestion = questionService.createQuestion(request, getAuthenticatedUser());
		return ResponseEntity.status(HttpStatus.CREATED).body(savedQuestion.getId());
	}

	private User getAuthenticatedUser() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = customUserDetailsService.findByUsername(userDetails.getUsername());
		return user;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<List<QuestionDTO>> getAllQuestions() {

		List<QuestionDTO> dtos = questionService.getAllQuestions().stream().map(QuestionDTO::fromEntity).toList();

		return ResponseEntity.ok(dtos);
	}

	@PutMapping("/{id}/{status}")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<QuestionDTO> updateStatusQuestion(@PathVariable("id") Integer id,
			@PathVariable("status") Question.QuestionStatus status) {
		Question updated = questionService.updateStatusQuestion(id, status);
		return ResponseEntity.ok(QuestionDTO.fromEntity(updated));
	}

}
