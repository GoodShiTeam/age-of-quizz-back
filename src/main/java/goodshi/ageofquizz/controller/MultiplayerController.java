package goodshi.ageofquizz.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.model.CreateRoomRequest;
import goodshi.ageofquizz.model.CreateRoomResponse;
import goodshi.ageofquizz.model.CreateRoomResult;
import goodshi.ageofquizz.model.JoinRoomRequest;
import goodshi.ageofquizz.model.Room;
import goodshi.ageofquizz.model.StartGameRequest;
import goodshi.ageofquizz.model.ReadyRequest;
import goodshi.ageofquizz.service.AuthenticationFacade;
import goodshi.ageofquizz.service.MultiplayerService;

@RestController
@RequestMapping("/multiplayer")
public class MultiplayerController {

	@Autowired
	private MultiplayerService multiplayerService;

	@Autowired
	private AuthenticationFacade authenticationFacade;

	@PostMapping("/rooms")
	public ResponseEntity<CreateRoomResponse> createRoom(@RequestBody(required = false) CreateRoomRequest request) {
		User user = authenticationFacade.getAuthenticatedUser();
		String displayName = request == null ? null : request.getDisplayName();
		CreateRoomResult result = multiplayerService.createRoomForHost(user, displayName);
		Room room = result.getRoom();
		CreateRoomResponse resp = new CreateRoomResponse(room.getCode(), room.getHost().getUsername(),
				room.getPlayers().stream().map(u -> u.getUsername()).collect(Collectors.toList()),
				result.getParticipantId());
		return ResponseEntity.status(HttpStatus.CREATED).body(resp);
	}

	@PostMapping("/rooms/join")
	public ResponseEntity<?> joinRoom(@RequestBody JoinRoomRequest req) {
		User user = authenticationFacade.getAuthenticatedUser();
		try {
			CreateRoomResult result = multiplayerService.joinRoom(req.getCode(), user, req.getParticipantId(),
					req.getDisplayName());
			Room room = result.getRoom();
			Map<String, Object> resp = new HashMap<>();
			resp.put("code", room.getCode());
			resp.put("players", room.getPlayers().stream().map(u -> u.getUsername()).collect(Collectors.toList()));
			resp.put("participantId", result.getParticipantId());
			return ResponseEntity.ok(resp);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/rooms/{code}/start")
	public ResponseEntity<?> startGame(@PathVariable String code, @RequestBody(required = false) StartGameRequest req) {
		User user = authenticationFacade.getAuthenticatedUser();
		String participantId = req == null ? null : req.getParticipantId();
		try {
			int numberOfQuestions = 5;
			multiplayerService.startGameIfHost(code, user, participantId, numberOfQuestions);
			return ResponseEntity.ok(Map.of("message", "Game started", "code", code));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		} catch (IllegalStateException e) {
			// used when requester is not host
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/rooms/{code}/players/ready")
	public ResponseEntity<?> setPlayerReady(@PathVariable String code, @RequestBody(required = false) ReadyRequest req) {
		User user = authenticationFacade.getAuthenticatedUser();
		String participantId = req == null ? null : req.getParticipantId();
		boolean ready = req == null ? true : req.isReady();
		try {
			multiplayerService.setParticipantReady(code, user, participantId, ready);
			return ResponseEntity.ok(Map.of("message", "ready state updated", "code", code, "ready", ready));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/rooms/{code}")
	public ResponseEntity<?> getRoom(@PathVariable String code) {
		return ResponseEntity.ok().body("Infos de la room " + code);
	}

	@GetMapping("/rooms/{code}/state")
	public ResponseEntity<?> getRoomState(@PathVariable String code) {
		Map<String, Object> state = multiplayerService.getRoomStateMap(code);
		return ResponseEntity.ok(state);
	}

}