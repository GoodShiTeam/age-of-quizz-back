package goodshi.ageofquizz.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.dto.NotificationDTO;
import goodshi.ageofquizz.entity.Notification;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.enums.RoleEnum;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.repository.NotificationRepository;
import goodshi.ageofquizz.repository.UserRepository;
import jakarta.transaction.Transactional;

/**
 * Notifications liées au cycle de vie des questions. On ne notifie jamais la
 * personne à l'origine de l'action.
 */
@Service
public class NotificationService {

	/** Rôles qui reçoivent les questions à relire (l'admin a tous les droits). */
	private static final List<RoleEnum> REVIEWER_ROLES = List.of(RoleEnum.REVIEWER, RoleEnum.ADMIN);

	/** Notifications « à relire », soldées dès qu'un relecteur a tranché. */
	private static final List<Notification.Type> REVIEW_TYPES = List.of(Notification.Type.QUESTION_TO_REVIEW,
			Notification.Type.QUESTION_RESUBMITTED);

	private static final int MAX_LISTED = 50;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	/** Nouvelle question ou question corrigée par son auteur : les relecteurs sont prévenus. */
	public void notifyReviewers(Question question, User actor, boolean resubmitted) {
		Notification.Type type = resubmitted ? Notification.Type.QUESTION_RESUBMITTED
				: Notification.Type.QUESTION_TO_REVIEW;
		List<Notification> notifications = userRepository.findByAnyRole(REVIEWER_ROLES).stream()
				.filter(reviewer -> !isSameUser(reviewer, actor))
				.map(reviewer -> new Notification(reviewer, question, type, displayName(actor))).toList();
		notificationRepository.saveAll(notifications);
	}

	/** Prévient l'auteur d'une action faite par quelqu'un d'autre sur sa question. */
	public void notifyAuthor(Question question, User actor, Notification.Type type) {
		User author = question.getAuthor();
		if (author == null || isSameUser(author, actor)) {
			return;
		}
		notificationRepository.save(new Notification(author, question, type, displayName(actor)));
	}

	/** La question a été tranchée : elle ne doit plus apparaître « à relire » chez les autres relecteurs. */
	public void clearReviewRequests(Question question) {
		notificationRepository.markReadForQuestion(question.getId(), REVIEW_TYPES, Instant.now());
	}

	/**
	 * Question signalée par un joueur : relecteurs et auteur sont prévenus (une
	 * seule notification si l'auteur est aussi relecteur), jamais le signaleur.
	 */
	public void notifyReported(Question question, User reporter) {
		Map<Long, User> recipients = new LinkedHashMap<>();
		userRepository.findByAnyRole(REVIEWER_ROLES).forEach(reviewer -> recipients.put(reviewer.getId(), reviewer));
		if (question.getAuthor() != null) {
			recipients.putIfAbsent(question.getAuthor().getId(), question.getAuthor());
		}
		List<Notification> notifications = recipients.values().stream()
				.filter(recipient -> !isSameUser(recipient, reporter))
				.map(recipient -> new Notification(recipient, question, Notification.Type.QUESTION_REPORTED,
						displayName(reporter)))
				.toList();
		notificationRepository.saveAll(notifications);
	}

	/** Signalements traités : leurs notifications sont soldées pour tout le monde. */
	public void clearReportNotifications(Integer questionId) {
		notificationRepository.markReadForQuestion(questionId, List.of(Notification.Type.QUESTION_REPORTED),
				Instant.now());
	}

	@Transactional
	public List<NotificationDTO> getLatest(User user) {
		return notificationRepository.findLatestForRecipient(user.getId(), PageRequest.of(0, MAX_LISTED)).stream()
				.map(NotificationDTO::fromEntity).toList();
	}

	public long countUnread(User user) {
		return notificationRepository.countByRecipientIdAndReadAtIsNull(user.getId());
	}

	@Transactional
	public void markRead(Long notificationId, User user) {
		Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Notification " + notificationId + " introuvable"));
		notification.markRead();
	}

	@Transactional
	public void markAllRead(User user) {
		notificationRepository.markAllRead(user.getId(), Instant.now());
	}

	private static boolean isSameUser(User a, User b) {
		return b != null && Objects.equals(a.getId(), b.getId());
	}

	private static String displayName(User user) {
		if (user == null) {
			return null;
		}
		if (user.getUserProfile() != null && user.getUserProfile().getPseudo() != null) {
			return user.getUserProfile().getPseudo();
		}
		return user.getUsername();
	}
}
