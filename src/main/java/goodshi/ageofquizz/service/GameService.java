package goodshi.ageofquizz.service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import goodshi.ageofquizz.controller.AnswerPayload;
import goodshi.ageofquizz.entity.Answer;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.entity.UserAnswer;
import goodshi.ageofquizz.model.GameEvent;
import goodshi.ageofquizz.model.GameRecapPayload;
import goodshi.ageofquizz.model.GameSessionMetadata;
import goodshi.ageofquizz.model.NewQuestionPayload;
import goodshi.ageofquizz.model.PlayerAnswerRecord;
import goodshi.ageofquizz.model.PlayerResult;
import goodshi.ageofquizz.model.QuestionDetail;
import goodshi.ageofquizz.model.Room;
import goodshi.ageofquizz.model.Room.RoomStatus;

@Service
public class GameService {

	@Autowired
	private MultiplayerService multiplayerService;

	@Autowired
	private MultiplayerEventService eventService;

	@Autowired
	private UserService userService;

	@Autowired
	private goodshi.ageofquizz.repository.UserAnswerRepository userAnswerRepository;

	@Autowired
	private goodshi.ageofquizz.repository.AnswerRepository answerRepository;

	@Autowired
	private goodshi.ageofquizz.repository.GameSessionRepository gameSessionRepository;

	@Autowired
	private goodshi.ageofquizz.repository.GameQuestionDetailRepository gameQuestionDetailRepository;

	@Autowired
	private goodshi.ageofquizz.repository.GamePlayerResultRepository gamePlayerResultRepository;

	@Autowired
	private goodshi.ageofquizz.repository.GameRecapSnapshotRepository gameRecapSnapshotRepository;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Process an answer for a given room. Returns an AnswerProcessingResult
	 * describing whether the answer was accepted and if it was correct. This method
	 * also broadcasts updated scores when appropriate.
	 *
	 * If the Principal is present, it is used to identify the user. Otherwise the
	 * client-provided userId in the payload is used if present. If neither is
	 * available, an anonymous transient id is generated so unauthenticated users
	 * can still play.
	 */
	public AnswerProcessingResult processAnswer(String code, AnswerPayload payload, Principal principal) {
		Room room = multiplayerService.getRoomOrThrow(code);

		// determine user/participant identity
		Long userId = null;
		String participantId = null;
		if (principal != null && principal.getName() != null) {
			try {
				User user = userService.findByUsername(principal.getName());
				userId = user.getId();
				participantId = "user:" + userId;
			} catch (Exception e) {
				userId = null;
			}
		}

		if ((userId == null || userId == -1L) && payload != null) {
			// prefer explicit participant id from payload for anonymous users
			try {
				java.lang.reflect.Method m = payload.getClass().getMethod("getParticipantId");
				Object val = m.invoke(payload);
				if (val instanceof String) {
					participantId = (String) val;
				}
			} catch (Exception ex) {
				// no participantId on payload, ignore
			}
			if (userId == null && payload != null) {
				if (payload.getUserId() != null) {
					userId = payload.getUserId();
					participantId = "user:" + userId;
				}
			}
		}

		// fallback: anonymous user without provided participantId -> create transient
		// participantId
		if (participantId == null) {
			participantId = "anon_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		}

		synchronized (room) {
			if (room.getStatus() != RoomStatus.IN_GAME) {
				return new AnswerProcessingResult(false, false, null, userId);
			}

			if (!room.isQuestionActive()) {
				return new AnswerProcessingResult(false, false, null, userId);
			}

			if (room.hasParticipantAnswered(participantId)) {
				return new AnswerProcessingResult(false, false, null, userId);
			}

			// ensure score entry exists for this participant
			if (!room.getScores().containsKey(participantId)) {
				room.getScores().put(participantId, 0);
			}

			Question current = room.getCurrentQuestion();
			AnswerEvaluation eval = evaluateAnswer(current, payload);
			boolean correct = eval.correct;
			List<String> selected = eval.selected;
			List<String> correctAnswers = eval.correctAnswers;

			long now = System.currentTimeMillis();
			long timeSeconds = Math.max(0L, (now - room.getQuestionStartTime()) / 1000L);

			String username = null;
			if (userId != null && userId != -1L) {
				try {
					User u = userService.getUser(userId);
					username = u.getUsername();
				} catch (Exception e) {
					username = "user:" + userId;
				}
			} else {
				// try to retrieve display name from room participants
				User p = room.getParticipantsByKey().get(participantId);
				if (p != null && p.getUsername() != null) {
					username = p.getUsername();
				} else {
					username = participantId;
				}
			}

			// record player answer
			PlayerAnswerRecord record = new PlayerAnswerRecord(current.getId(), userId, participantId, username,
					selected, correctAnswers, correct, timeSeconds);
			room.addPlayerAnswer(current.getId(), participantId, record);

			// persist to user_answer – one row per selected answer
			try {
				User dbUser = userService.getUser(userId);
				for (Integer aid : eval.selectedIds) {
					Answer dbAnswer = answerRepository.findById(aid).orElse(null);
					if (dbAnswer != null) {
						UserAnswer ua = new UserAnswer();
						ua.setUser(dbUser);
						ua.setQuestion(current);
						ua.setAnswer(dbAnswer);
						ua.setResponseTimeSeconds(new BigDecimal(timeSeconds));
						ua.setAnsweredAt(LocalDateTime.now());
						userAnswerRepository.save(ua);
					}
				}
			} catch (Exception e) {
				// ignore persistence errors; primary game loop should continue
				e.printStackTrace();
			}

			// mark as answered for this question (prevents multiple submissions)
			room.markParticipantAnswered(participantId);

			if (correct) {
				room.incrementScore(participantId);
				eventService.sendToRoom(code, new GameEvent("SCORES_UPDATED", room.getScores()));
			}

			// if all players have answered, immediately advance to next question
			boolean allAnswered = true;
			for (String pk : room.getParticipantsByKey().keySet()) {
				if (!room.hasParticipantAnswered(pk)) {
					allAnswered = false;
					break;
				}
			}

			if (allAnswered) {
				// advance room state to next question
				room.setQuestionActive(false);
				room.setCurrentQuestionIndex(room.getCurrentQuestionIndex() + 1);

				if (room.getCurrentQuestionIndex() >= room.getQuestions().size()) {
					room.setStatus(RoomStatus.FINISHED);
					room.setGameEndTime(System.currentTimeMillis());
					GameRecapPayload recap = buildRecap(room);
					eventService.sendToRoom(code, new GameEvent("GAME_FINISHED", recap));
				} else {
					long start = System.currentTimeMillis();
					room.setQuestionStartTime(start);
					room.setQuestionEndTime(start + room.getQuestionDuration() * 1000L);
					room.clearAnsweredUsers();
					room.setQuestionActive(true);

					NewQuestionPayload nextPayload = new NewQuestionPayload(room.getCurrentQuestion(),
							room.getQuestionStartTime(), room.getQuestionEndTime());
					eventService.sendToRoom(code, new GameEvent("NEW_QUESTION", nextPayload));
				}
			}

			return new AnswerProcessingResult(true, correct, room.getScores(), userId);
		}
	}

	/**
	 * Evaluates the answer submitted by a player against the current question.
	 * The answer is considered correct only if the player selected EXACTLY all
	 * correct answers – no more, no less.
	 */
	private AnswerEvaluation evaluateAnswer(Question currentQuestion, AnswerPayload playerAnswer) {
		// Build the set of correct answer IDs and their display values
		List<Integer> correctIds = new ArrayList<>();
		List<String> correctAnswerValues = new ArrayList<>();
		for (Answer a : currentQuestion.getAnswers()) {
			if (Boolean.TRUE.equals(a.isCorrect())) {
				correctIds.add(a.getId());
				correctAnswerValues.add(a.getValue());
			}
		}

		// Resolve the IDs submitted by the player
		List<Integer> submittedIds = new ArrayList<>();
		if (playerAnswer != null) {
			if (playerAnswer.getAnswerIds() != null && !playerAnswer.getAnswerIds().isEmpty()) {
				submittedIds.addAll(playerAnswer.getAnswerIds());
			} else if (playerAnswer.getAnswerId() != null) {
				// backward-compat: single answerId
				submittedIds.add(playerAnswer.getAnswerId());
			}
		}

		// Build the display list of selected values (resolved from question answers)
		List<String> selected = new ArrayList<>();
		List<Integer> resolvedSelectedIds = new ArrayList<>();
		if (!submittedIds.isEmpty()) {
			for (Answer a : currentQuestion.getAnswers()) {
				if (submittedIds.contains(a.getId())) {
					selected.add(a.getValue());
					resolvedSelectedIds.add(a.getId());
				}
			}
		} else if (playerAnswer != null && playerAnswer.getAnswerValue() != null) {
			// fallback: match by value (single)
			String v = playerAnswer.getAnswerValue().trim();
			for (Answer a : currentQuestion.getAnswers()) {
				if (a.getValue().equalsIgnoreCase(v)) {
					selected.add(a.getValue());
					resolvedSelectedIds.add(a.getId());
					break;
				}
			}
		}

		// Correct only when the player selected exactly the full set of correct answers
		boolean correct = !resolvedSelectedIds.isEmpty()
				&& resolvedSelectedIds.size() == correctIds.size()
				&& resolvedSelectedIds.containsAll(correctIds);

		return new AnswerEvaluation(correct, selected, correctAnswerValues, resolvedSelectedIds);
	}

	/** Holds the result of evaluating a player's answer against a question. */
	private static class AnswerEvaluation {
		final boolean correct;
		final List<String> selected;
		final List<String> correctAnswers;
		final List<Integer> selectedIds;

		AnswerEvaluation(boolean correct, List<String> selected, List<String> correctAnswers, List<Integer> selectedIds) {
			this.correct = correct;
			this.selected = selected;
			this.correctAnswers = correctAnswers;
			this.selectedIds = selectedIds;
		}
	}

	/**
	 * Build a detailed recap payload for the finished room following the requested
	 * schema.
	 */
	public GameRecapPayload buildRecap(Room room) {
		GameRecapPayload payload = new GameRecapPayload();

		// session metadata
		GameSessionMetadata meta = new GameSessionMetadata();
		meta.setGameSessionId(room.getGameSessionId());
		meta.setGameMode("MULTIPLAYER");
		meta.setStatus("FINISHED");
		meta.setStartTime(Instant.ofEpochMilli(room.getGameStartTime()).toString());
		long end = room.getGameEndTime() > 0 ? room.getGameEndTime() : System.currentTimeMillis();
		meta.setEndTime(Instant.ofEpochMilli(end).toString());
		meta.setTotalDuration((end - room.getGameStartTime()) / 1000L);
		meta.setQuestionCount(room.getQuestions().size());
		payload.setSession(meta);

		// per-player aggregation
		List<PlayerResult> results = new ArrayList<>();
		int totalQuestions = room.getQuestions().size();

		// compute points and times from recorded answers keyed by participantKey
		// (String)
		Map<String, Integer> pointsByParticipant = new HashMap<>();
		Map<String, Long> timeByParticipant = new HashMap<>();
		Map<String, Integer> correctCountByParticipant = new HashMap<>();

		for (Map.Entry<Integer, Map<String, PlayerAnswerRecord>> e : room.getQuestionResponses().entrySet()) {
			for (Map.Entry<String, PlayerAnswerRecord> ent : e.getValue().entrySet()) {
				String participantKey = ent.getKey();
				PlayerAnswerRecord r = ent.getValue();
				int pts = 0;
				if (r.isCorrect()) {
					// simple points: 30 - timeSeconds, min 10
					pts = Math.max(10, (int) (30 - r.getTimeSeconds()));
				}
				pointsByParticipant.put(participantKey, pointsByParticipant.getOrDefault(participantKey, 0) + pts);
				timeByParticipant.put(participantKey,
						timeByParticipant.getOrDefault(participantKey, 0L) + r.getTimeSeconds());
				if (r.isCorrect()) {
					correctCountByParticipant.put(participantKey,
							correctCountByParticipant.getOrDefault(participantKey, 0) + 1);
				}
			}
		}

		// ensure all participants included
		for (Map.Entry<String, User> entry : room.getParticipantsByKey().entrySet()) {
			String pk = entry.getKey();
			User p = entry.getValue();
			Long uid = p.getId();
			int score = room.getScores().getOrDefault(pk, 0);
			int points = pointsByParticipant.getOrDefault(pk, 0);
			int correctAnswers = correctCountByParticipant.getOrDefault(pk, 0);
			long totalTime = timeByParticipant.getOrDefault(pk, 0L);
			double avg = totalQuestions > 0 ? ((double) totalTime) / totalQuestions : 0.0;
			double accuracy = totalQuestions > 0 ? ((double) correctAnswers / totalQuestions) * 100.0 : 0.0;

			PlayerResult pr = new PlayerResult();
			pr.setUserId(uid);
			pr.setUsername(p.getUsername());
			pr.setScore(score);
			pr.setPoints(points);
			pr.setCorrectAnswers(correctAnswers);
			pr.setTotalQuestions(totalQuestions);
			pr.setTotalTimeSeconds(totalTime);
			pr.setAverageTimePerQuestion(avg);
			pr.setAccuracy(Math.round(accuracy * 10.0) / 10.0);
			pr.setAvatar(p.getUserProfile() != null ? p.getUserProfile().getAvatar() : null);
			results.add(pr);
		}

		// sort by points desc then score
		Collections.sort(results, Comparator.comparingInt(PlayerResult::getPoints).reversed()
				.thenComparing(Comparator.comparingInt(PlayerResult::getScore).reversed()));

		// assign ranks
		int rank = 1;
		for (PlayerResult pr : results) {
			pr.setRank(rank++);
		}

		Map<String, Object> resultsMap = new HashMap<>();
		resultsMap.put("playerResults", results);
		payload.setResults(resultsMap);

		// question details
		List<QuestionDetail> qdetails = new ArrayList<>();
		for (Question q : room.getQuestions()) {
			QuestionDetail qd = new QuestionDetail();
			qd.setQuestionId(q.getId());
			qd.setQuestionText(q.getLibelle());
			Map<String, PlayerAnswerRecord> map = room.getQuestionResponses().get(q.getId());
			Map<String, Object> prs = new HashMap<>();
			if (map != null) {
				for (PlayerAnswerRecord r : map.values()) {
					Map<String, Object> rmap = new HashMap<>();
					rmap.put("selected", r.getSelected());
					rmap.put("correct", r.getCorrect());
					rmap.put("isCorrect", r.isCorrect());
					rmap.put("timeSeconds", r.getTimeSeconds());
					prs.put(r.getUsername(), rmap);
				}
			}
			qd.setPlayerResponses(prs);
			qdetails.add(qd);
		}
		payload.setQuestions(qdetails);

		// simple statistics
		Map<String, Object> stats = new HashMap<>();
		Long fastestUid = null;
		long fastest = Long.MAX_VALUE;
		Long slowestUid = null;
		long slowest = -1L;
		long totalTimeAll = 0L;
		int countTimes = 0;
		for (Map<String, PlayerAnswerRecord> map : room.getQuestionResponses().values()) {
			for (PlayerAnswerRecord r : map.values()) {
				long t = r.getTimeSeconds();
				totalTimeAll += t;
				countTimes++;
				if (t < fastest) {
					fastest = t;
					fastestUid = r.getUserId();
				}
				if (t > slowest) {
					slowest = t;
					slowestUid = r.getUserId();
				}
			}
		}
		if (countTimes > 0) {
			Map<String, Object> fastestMap = new HashMap<>();
			fastestMap.put("userId", fastestUid);
			fastestMap.put("timeSeconds", fastest);
			Map<String, Object> slowestMap = new HashMap<>();
			slowestMap.put("userId", slowestUid);
			slowestMap.put("timeSeconds", slowest);
			stats.put("fastestAnswerer", fastestMap);
			stats.put("slowestAnswerer", slowestMap);
			stats.put("averageTimeAllPlayers", countTimes > 0 ? ((double) totalTimeAll) / countTimes : 0.0);
		}
		payload.setStatistics(stats);

		payload.setAllowRematch(true);
		payload.setRematchExpireIn(300);

		// persist session + details + results + snapshot
		try {
			// session entity
			goodshi.ageofquizz.entity.GameSessionEntity sessionEntity = new goodshi.ageofquizz.entity.GameSessionEntity();
			sessionEntity.setGameSessionId(meta.getGameSessionId());
			sessionEntity.setRoomCode(room.getCode());
			sessionEntity.setGameMode(meta.getGameMode());
			sessionEntity.setStatus(meta.getStatus());
			sessionEntity.setStartTime(Instant.ofEpochMilli(room.getGameStartTime()));
			sessionEntity.setEndTime(Instant.ofEpochMilli(end));
			sessionEntity.setTotalDurationS((int) meta.getTotalDuration());
			sessionEntity.setQuestionCount(meta.getQuestionCount());
			gameSessionRepository.save(sessionEntity);

			// question details
			for (Question q : room.getQuestions()) {
				goodshi.ageofquizz.entity.GameQuestionDetailEntity qent = new goodshi.ageofquizz.entity.GameQuestionDetailEntity();
				qent.setGameSessionId(meta.getGameSessionId());
				qent.setQuestion(q);
				qent.setQuestionOrder(room.getQuestions().indexOf(q) + 1);
				qent.setQuestionText(q.getLibelle());
				// correct answers as JSON array
				List<String> correct = new ArrayList<>();
				for (Answer a : q.getAnswers()) {
					if (Boolean.TRUE.equals(a.isCorrect())) {
						correct.add(a.getValue());
					}
				}
				qent.setCorrectAnswers(objectMapper.writeValueAsString(correct));
				gameQuestionDetailRepository.save(qent);
			}

			// player results
			for (PlayerResult pr : results) {
				goodshi.ageofquizz.entity.GamePlayerResultEntity pent = new goodshi.ageofquizz.entity.GamePlayerResultEntity();
				pent.setGameSessionId(meta.getGameSessionId());
				try {
					pent.setUser(userService.getUser(pr.getUserId()));
				} catch (Exception e) {
					// user may be anonymous (-1) or missing, leave user null and keep username
				}
				pent.setUsername(pr.getUsername());
				pent.setRank(pr.getRank());
				pent.setScore(pr.getScore());
				pent.setPoints(pr.getPoints());
				pent.setCorrectAnswers(pr.getCorrectAnswers());
				pent.setTotalQuestions(pr.getTotalQuestions());
				pent.setAccuracy(pr.getAccuracy());
				pent.setTotalTimeSeconds(pr.getTotalTimeSeconds());
				pent.setAverageTimePerQuestion(pr.getAverageTimePerQuestion());
				pent.setAvatar(pr.getAvatar());
				gamePlayerResultRepository.save(pent);
			}

			// snapshot JSON
			String recapJson = objectMapper.writeValueAsString(payload);
			goodshi.ageofquizz.entity.GameRecapSnapshotEntity snap = new goodshi.ageofquizz.entity.GameRecapSnapshotEntity();
			snap.setGameSessionId(meta.getGameSessionId());
			snap.setRecapJson(recapJson);
			gameRecapSnapshotRepository.save(snap);
		} catch (Exception e) {
			// do not block return on persistence errors; log if logger available
			e.printStackTrace();
		}

		return payload;
	}

}
