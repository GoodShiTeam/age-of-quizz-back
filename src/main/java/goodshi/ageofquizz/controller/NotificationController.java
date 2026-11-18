package goodshi.ageofquizz.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.dto.NotificationDTO;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.service.AuthenticationFacade;
import goodshi.ageofquizz.service.NotificationService;

/** Notifications de l'utilisateur connecté. */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private AuthenticationFacade authenticationFacade;

	@GetMapping
	public ResponseEntity<List<NotificationDTO>> getNotifications() {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(notificationService.getLatest(user));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<Map<String, Long>> getUnreadCount() {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(Map.of("count", notificationService.countUnread(user)));
	}

	@PutMapping("/{id}/read")
	public ResponseEntity<Void> markRead(@PathVariable("id") Long id) {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		notificationService.markRead(id, user);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/read-all")
	public ResponseEntity<Void> markAllRead() {
		User user = authenticationFacade.getAuthenticatedUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		notificationService.markAllRead(user);
		return ResponseEntity.noContent().build();
	}
}
