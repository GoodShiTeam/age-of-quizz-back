package goodshi.ageofquizz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.model.GameEvent;
import goodshi.ageofquizz.model.CreateRoomResult;
import goodshi.ageofquizz.model.NewQuestionPayload;
import goodshi.ageofquizz.model.Room;
import goodshi.ageofquizz.model.Room.RoomStatus;
import goodshi.ageofquizz.repository.QuestionRepository;

@Service
public class MultiplayerService {

	private final Map<String, Room> rooms = new ConcurrentHashMap<>();

	@Autowired
	private MultiplayerEventService eventService;

	@Autowired
	private QuestionRepository questionRepository;

	/**
	 * Create a room for host. If host == null, an anonymous participantId is
	 * generated and the provided displayName (if any) is used as username for
	 * display purposes. Returns CreateRoomResult containing the room and the
	 * participantId (non-null only for anonymous hosts).
	 */
	public CreateRoomResult createRoomForHost(User host, String displayName) {
		// allow anonymous host: if no authenticated user provided, create transient anonymous user with id -1
		if (host == null) {
			// generate a stable participant id for the client to persist (short UUID)
			String participantId = "anon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
			User anon = new User();
			// keep the DB anonymous user id (-1) as requested
			anon.setId(-1L);
			// use provided displayName when available, else fall back to participantId
			String username = participantId;
			if (displayName != null && !displayName.trim().isEmpty()) {
				username = displayName.trim();
			}
			// username carries the display name seen by other players
			anon.setUsername(username);
			host = anon;

			String code = generateRoomCode();
			// ensure unique code
			while (rooms.containsKey(code)) {
				code = generateRoomCode();
			}
			Room room = createRoom(code, host, participantId);
			return new CreateRoomResult(room, participantId);
		}
		String code = generateRoomCode();
		// ensure unique code
		while (rooms.containsKey(code)) {
			code = generateRoomCode();
		}
		Room room = createRoom(code, host, null);
		return new CreateRoomResult(room, null);
	}

	private String generateRoomCode() {
		String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // avoid confusing chars
		StringBuilder sb = new StringBuilder(6);
		for (int i = 0; i < 6; i++) {
			sb.append(chars.charAt((int) (Math.random() * chars.length())));
		}
		return sb.toString();
	}

	/**
	 * Start game for a room and automatically fetch a number of random validated
	 * questions. Only basic validation is performed here (room existence).
	 */
	public Room startGameWithRandomQuestions(String code, int numberOfQuestions) {
		List<Question> questions = questionRepository.findRandomQuestions(Question.QuestionStatus.VALIDATED, null,
				org.springframework.data.domain.PageRequest.of(0, numberOfQuestions));
		List<Integer> ids = questions.stream().map(goodshi.ageofquizz.entity.Question::getId).toList();
		List<Question> questionsWithAnswers = questionRepository.findByIdsWithAnswers(ids);
		return startGame(code, questionsWithAnswers);
	}

	/**
	 * Start game but only if the requester is the host. Throws
	 * IllegalStateException if not allowed.
	 */
	public Room startGameIfHost(String code, User requester, String participantId, int numberOfQuestions) {
		Room room = getRoomOrThrow(code);
		boolean allowed = false;
		if (requester != null && requester.getId() != null) {
			allowed = room.getHost().getId().equals(requester.getId());
		} else if (participantId != null) {
			String hostKey = room.getParticipantKeyForHost();
			allowed = participantId.equals(hostKey);
		}
		if (!allowed) {
			throw new IllegalStateException("Only host can start the game");
		}
		return startGameWithRandomQuestions(code, numberOfQuestions);
	}

	/**
	 * Set/unset a participant as ready in a room and notify other clients.
	 */
	public void setParticipantReady(String code, User user, String participantId, boolean ready) {
		Room room = getRoomOrThrow(code);
		String participantKey;
		if (user != null && user.getId() != null && user.getId() != -1L) {
			participantKey = "user:" + user.getId();
		} else {
			if (participantId == null) {
				throw new IllegalArgumentException("Anonymous participantId required");
			}
			participantKey = participantId;
		}
		// ensure participant exists in room
		if (!room.getParticipantsByKey().containsKey(participantKey)) {
			throw new IllegalArgumentException("Participant not in room");
		}
		room.setParticipantReady(participantKey, ready);

		// Build a lightweight payload: participantKey, username, ready
		Map<String, Object> payload = new HashMap<>();
		payload.put("participantKey", participantKey);
		User p = room.getParticipantsByKey().get(participantKey);
		payload.put("username", p == null ? null : p.getUsername());
		payload.put("ready", ready);

		eventService.sendToRoom(code, new GameEvent("PLAYER_READY", payload));
	}

	/**
	 * Build a simple state map representing the room. Controller can return this
	 * directly.
	 */
	public Map<String, Object> getRoomStateMap(String code) {
		Room room = getRoomOrThrow(code);
		Map<String, Object> state = new HashMap<>();
		state.put("code", room.getCode());
		state.put("status", room.getStatus());
		state.put("players", room.getPlayers().stream().map(u -> u.getUsername()).collect(Collectors.toList()));
		state.put("scores", room.getScores());
		state.put("currentQuestionIndex", room.getCurrentQuestionIndex());
		state.put("questionActive", room.isQuestionActive());
		state.put("questionStartTime", room.getQuestionStartTime());
		state.put("questionEndTime", room.getQuestionEndTime());
		// include readiness per participant (username -> boolean)
		Map<String, Boolean> readyMap = new HashMap<>();
		for (Map.Entry<String, User> e : room.getParticipantsByKey().entrySet()) {
			String key = e.getKey();
			String username = e.getValue() == null ? key : e.getValue().getUsername();
			readyMap.put(username, room.isParticipantReady(key));
		}
		state.put("ready", readyMap);
		if (room.getCurrentQuestion() != null) {
			state.put("currentQuestionId", room.getCurrentQuestion().getId());
			state.put("currentQuestionLibelle", room.getCurrentQuestion().getLibelle());
		}
		return state;
	}

	private Room createRoom(String code, User host, String hostParticipantKey) {
		Room room = new Room(code, host, hostParticipantKey);
		rooms.put(code, room);
		return room;
	}

	public CreateRoomResult joinRoom(String code, User user, String participantId, String displayName) {
		Room room = getRoomOrThrow(code);
		if (user != null) {
			room.addPlayer(user);
			notifyPlayerJoined(room, user.getUsername());
			return new CreateRoomResult(room, null);
		}
		if (participantId == null || participantId.trim().isEmpty()) {
			participantId = "anon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		}
		// create transient anonymous user for display purposes
		User anon = new User();
		anon.setId(-1L);
		anon.setUsername(displayName == null || displayName.trim().isEmpty() ? participantId : displayName.trim());
		room.addPlayer(anon, participantId);
		notifyPlayerJoined(room, anon.getUsername());
		return new CreateRoomResult(room, participantId);
	}

	private void notifyPlayerJoined(Room room, String username) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("username", username);
		payload.put("players", room.getPlayers().stream().map(User::getUsername).collect(Collectors.toList()));
		eventService.sendToRoom(room.getCode(), new GameEvent("PLAYER_JOINED", payload));
	}

	public Room startGame(String code, List<Question> questions) {
		Room room = getRoomOrThrow(code);

		room.setQuestions(questions);
		// initialize session metadata
		room.setGameSessionId(java.util.UUID.randomUUID().toString());
		long gameStart = System.currentTimeMillis();
		room.setGameStartTime(gameStart);
		room.setStatus(RoomStatus.IN_GAME);

		room.setCurrentQuestionIndex(0);
		// clear previous per-question responses and answered state (important for rematches)
		room.getQuestionResponses().clear();
		room.clearAnsweredUsers();
		long start = System.currentTimeMillis();
		room.setQuestionStartTime(start);
		room.setQuestionEndTime(start + room.getQuestionDuration() * 1000L);
		room.setQuestionActive(true);

		eventService.sendToRoom(code, new GameEvent("GAME_STARTED", null));

		NewQuestionPayload payload = new NewQuestionPayload(room.getCurrentQuestion(), room.getQuestionStartTime(),
				room.getQuestionEndTime());
		eventService.sendToRoom(code, new GameEvent("NEW_QUESTION", payload));

		return room;
	}

	public Map<String, Room> getRooms() {
		return rooms;
	}

	public Room getRoomOrThrow(String code) {
		Room room = rooms.get(code);
		if (room == null) {
			throw new IllegalArgumentException("Room not found: " + code);
		}
		return room;
	}

}
