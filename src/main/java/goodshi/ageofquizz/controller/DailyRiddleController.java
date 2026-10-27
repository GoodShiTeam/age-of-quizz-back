package goodshi.ageofquizz.controller;

import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import goodshi.ageofquizz.dto.DailyRiddleDTO;
import goodshi.ageofquizz.dto.DailyRiddleEntryDTO;
import goodshi.ageofquizz.dto.DailyRiddleEntryRequestDTO;
import goodshi.ageofquizz.dto.DailyRiddleGuessRequest;
import goodshi.ageofquizz.dto.DailyRiddleGuessResponse;
import goodshi.ageofquizz.dto.DailyRiddleImportResultDTO;
import goodshi.ageofquizz.dto.DailyRiddleStatsDTO;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.service.AuthenticationFacade;
import goodshi.ageofquizz.service.DailyRiddleAdminService;
import goodshi.ageofquizz.service.DailyRiddleService;

@RestController
@RequestMapping("/daily-riddle")
public class DailyRiddleController {

	@Autowired
	private DailyRiddleService dailyRiddleService;

	@Autowired
	private DailyRiddleAdminService dailyRiddleAdminService;

	@Autowired
	private AuthenticationFacade authenticationFacade;

	/** Public ; si un JWT est fourni, la progression enregistrée est renvoyée. */
	@GetMapping
	public ResponseEntity<DailyRiddleDTO> getDailyRiddle(@RequestParam(defaultValue = "1") int revealed) {
		return ResponseEntity.ok(dailyRiddleService.getRiddle(revealed, authenticationFacade.getAuthenticatedUser()));
	}

	@GetMapping("/stats")
	public ResponseEntity<DailyRiddleStatsDTO> getStats() {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(dailyRiddleService.getStats(user));
	}

	@PostMapping("/guess")
	public ResponseEntity<DailyRiddleGuessResponse> guess(@RequestBody DailyRiddleGuessRequest request) {
		if (request.guess() == null || request.attempt() < 1
				|| request.attempt() > DailyRiddleService.MAX_ATTEMPTS) {
			return ResponseEntity.badRequest().build();
		}
		// L'énigme a changé depuis que le joueur l'a chargée (passage à minuit).
		if (!dailyRiddleService.today().equals(request.date())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		return ResponseEntity.ok(dailyRiddleService.guess(request.guess(), request.attempt(),
				authenticationFacade.getAuthenticatedUser()));
	}

	@GetMapping("/admin/entries")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<List<DailyRiddleEntryDTO>> listEntries() {
		return ResponseEntity.ok(dailyRiddleAdminService.listEntries());
	}

	@PostMapping("/admin/entries")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<DailyRiddleEntryDTO> createEntry(@RequestBody DailyRiddleEntryRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dailyRiddleAdminService.createEntry(request));
	}

	@PutMapping("/admin/entries/{id}")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<DailyRiddleEntryDTO> updateEntry(@PathVariable int id,
			@RequestBody DailyRiddleEntryRequestDTO request) {
		return ResponseEntity.ok(dailyRiddleAdminService.updateEntry(id, request));
	}

	/**
	 * Import depuis SiegeEngineers/aoe2techtree : {@code data} = data/data.json,
	 * {@code strings} = data/locales/fr/strings.json.
	 */
	@PostMapping("/admin/import")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<DailyRiddleImportResultDTO> importTechTree(@RequestParam("data") MultipartFile data,
			@RequestParam("strings") MultipartFile strings,
			@RequestParam(name = "keepManualEdits", defaultValue = "true") boolean keepManualEdits)
			throws IOException {
		try (InputStream dataIn = data.getInputStream(); InputStream stringsIn = strings.getInputStream()) {
			return ResponseEntity.ok(dailyRiddleAdminService.importFromTechTree(dataIn, stringsIn, keepManualEdits));
		}
	}
}
