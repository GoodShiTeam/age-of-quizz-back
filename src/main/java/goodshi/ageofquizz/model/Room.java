package goodshi.ageofquizz.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.User;

public class Room {

	private String code;
	private User host;

	private List<User> players = new ArrayList<>();

	private List<Question> questions = new ArrayList<>();

	private int currentQuestionIndex = 0;

	private long questionStartTime;
	private long questionEndTime;
	private int questionDuration = 15; // secondes
	private boolean questionActive = false;

	// use participantKey (String) as key: for authenticated users "user:{id}", for
	// guests the participantId (anon_xxx)
	private Map<String, Integer> scores = new HashMap<>();
	private Set<String> answeredParticipantIds = new HashSet<>();
	// participants who signalled they are ready (participantKey strings)
	private Set<String> readyParticipantIds = new HashSet<>();
	// questionId -> (participantKey -> record)
	private Map<Integer, Map<String, PlayerAnswerRecord>> questionResponses = new HashMap<>();

	// mapping participantKey -> User object (keeps displayName and other transient
	// info)
	private Map<String, User> participantsByKey = new HashMap<>();

	private String gameSessionId;
	private long gameStartTime;
	private long gameEndTime;

	private RoomStatus status = RoomStatus.WAITING;

	public enum RoomStatus {

		WAITING, IN_GAME, FINISHED

	}

	public Room(String code, User host, String hostParticipantKey) {
		this.code = code;
		this.host = host;
		this.players.add(host);
		if (hostParticipantKey == null) {
			hostParticipantKey = "user:" + host.getId();
		}
		this.participantsByKey.put(hostParticipantKey, host);
		this.scores.put(hostParticipantKey, 0);
	}

	public Question getCurrentQuestion() {
		if (questions.isEmpty()) {
			return null;
		}
		return questions.get(currentQuestionIndex);
	}

	public Map<Integer, Map<String, PlayerAnswerRecord>> getQuestionResponses() {
		return questionResponses;
	}

	public void addPlayerAnswer(Integer questionId, String participantKey, PlayerAnswerRecord record) {
		questionResponses.computeIfAbsent(questionId, k -> new HashMap<>()).put(participantKey, record);
	}

	public String getGameSessionId() {
		return gameSessionId;
	}

	public void setGameSessionId(String gameSessionId) {
		this.gameSessionId = gameSessionId;
	}

	public long getGameStartTime() {
		return gameStartTime;
	}

	public void setGameStartTime(long gameStartTime) {
		this.gameStartTime = gameStartTime;
	}

	public long getGameEndTime() {
		return gameEndTime;
	}

	public void setGameEndTime(long gameEndTime) {
		this.gameEndTime = gameEndTime;
	}

	public void addPlayer(User user) {
		// convenience: infer participant key from user id; this will not work for
		// guests with id -1
		players.add(user);
		String key = user.getId() != null && user.getId() != -1L ? "user:" + user.getId() : user.getUsername();
		participantsByKey.put(key, user);
		scores.put(key, 0);
	}

	public void addPlayer(User user, String participantKey) {
		players.add(user);
		String key = participantKey == null ? (user.getId() != null ? "user:" + user.getId() : user.getUsername())
				: participantKey;
		participantsByKey.put(key, user);
		scores.put(key, 0);
	}

	public boolean hasParticipantAnswered(String participantKey) {
		return answeredParticipantIds.contains(participantKey);
	}

	public void markParticipantAnswered(String participantKey) {
		answeredParticipantIds.add(participantKey);
	}

	public void clearAnsweredUsers() {
		answeredParticipantIds.clear();
	}

	public void setParticipantReady(String participantKey, boolean ready) {
		if (ready) {
			readyParticipantIds.add(participantKey);
		} else {
			readyParticipantIds.remove(participantKey);
		}
	}

	public boolean isParticipantReady(String participantKey) {
		return readyParticipantIds.contains(participantKey);
	}

	public Set<String> getReadyParticipantIds() {
		return readyParticipantIds;
	}

	public void incrementScore(String participantKey) {
		scores.put(participantKey, scores.getOrDefault(participantKey, 0) + 1);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public User getHost() {
		return host;
	}

	public void setHost(User host) {
		this.host = host;
	}

	public List<User> getPlayers() {
		return players;
	}

	public void setPlayers(List<User> players) {
		this.players = players;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public int getCurrentQuestionIndex() {
		return currentQuestionIndex;
	}

	public void setCurrentQuestionIndex(int currentQuestionIndex) {
		this.currentQuestionIndex = currentQuestionIndex;
	}

	public Map<String, Integer> getScores() {
		return scores;
	}

	public void setScores(Map<String, Integer> scores) {
		this.scores = scores;
	}

	public RoomStatus getStatus() {
		return status;
	}

	public void setStatus(RoomStatus status) {
		this.status = status;
	}

	public String getParticipantKeyForHost() {
		for (Map.Entry<String, User> e : participantsByKey.entrySet()) {
			if (e.getValue() == this.host) {
				return e.getKey();
			}
		}
		// fallback
		return "user:" + host.getId();
	}

	public Map<String, User> getParticipantsByKey() {
		return participantsByKey;
	}

	public boolean isTimeUp() {
		return System.currentTimeMillis() > questionEndTime;
	}

	public boolean isQuestionActive() {
		return questionActive;
	}

	public long getQuestionStartTime() {
		return questionStartTime;
	}

	public void setQuestionStartTime(long questionStartTime) {
		this.questionStartTime = questionStartTime;
	}

	public long getQuestionEndTime() {
		return questionEndTime;
	}

	public void setQuestionEndTime(long questionEndTime) {
		this.questionEndTime = questionEndTime;
	}

	public int getQuestionDuration() {
		return questionDuration;
	}

	public void setQuestionDuration(int questionDuration) {
		this.questionDuration = questionDuration;
	}

	public void setQuestionActive(boolean questionActive) {
		this.questionActive = questionActive;
	}

}