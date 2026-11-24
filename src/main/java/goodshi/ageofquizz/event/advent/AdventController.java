package goodshi.ageofquizz.event.advent;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.event.advent.AdventDtos.AnswerRequest;
import goodshi.ageofquizz.event.advent.AdventDtos.CalendarDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.DayDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.LeaderboardEntryDTO;
import goodshi.ageofquizz.event.advent.AdventDtos.ResultDTO;
import goodshi.ageofquizz.event.advent.AdventService.AdventException;
import goodshi.ageofquizz.service.AuthenticationFacade;

/**
 * Calendrier de l'avent. Le calendrier et le classement sont publics ; ouvrir
 * une case et y répondre demandent d'être connecté.
 */
@RestController
@RequestMapping("/events/advent")
public class AdventController {

	private final AdventService adventService;
	private final AuthenticationFacade authenticationFacade;

	public AdventController(AdventService adventService, AuthenticationFacade authenticationFacade) {
		this.adventService = adventService;
		this.authenticationFacade = authenticationFacade;
	}

	@GetMapping
	public ResponseEntity<CalendarDTO> getCalendar() {
		return ResponseEntity.ok(adventService.getCalendar(authenticationFacade.getAuthenticatedUser()));
	}

	@GetMapping("/leaderboard")
	public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard() {
		return ResponseEntity.ok(adventService.getLeaderboard(authenticationFacade.getAuthenticatedUser()));
	}

	@GetMapping("/days/{day}")
	public ResponseEntity<DayDTO> getDay(@PathVariable int day) {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(adventService.getDay(day, user));
	}

	@PostMapping("/days/{day}/answer")
	public ResponseEntity<ResultDTO> answer(@PathVariable int day, @RequestBody AnswerRequest request) {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (request == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(adventService.answer(day, request.answer(), request.useJoker(), user));
	}

	/** Révèle la bonne réponse d'une case ratée (elle ne pourra plus être rejouée). */
	@PostMapping("/days/{day}/reveal")
	public ResponseEntity<ResultDTO> reveal(@PathVariable int day) {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(adventService.reveal(day, user));
	}

	/** Mode test admin : efface ses réponses pour rejouer toutes les cases. */
	@DeleteMapping("/preview/answers")
	public ResponseEntity<Void> resetPreview() {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		adventService.resetPreview(user);
		return ResponseEntity.noContent().build();
	}

	/** Prioritaire sur GlobalExceptionHandler, qui renverrait tout en 409. */
	@ExceptionHandler(AdventException.class)
	public ResponseEntity<String> handleAdventException(AdventException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
	}
}
