package goodshi.ageofquizz.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Notification adressée à un utilisateur à propos d'une question : décision
 * d'un relecteur pour l'auteur, ou question à relire pour les relecteurs.
 */
@Entity
@Table(name = "notification")
public class Notification {

	public enum Type {
		/** Pour l'auteur : sa question a été validée. */
		QUESTION_VALIDATED,
		/** Pour l'auteur : sa question a été rejetée. */
		QUESTION_REJECTED,
		/** Pour l'auteur : quelqu'un d'autre a modifié sa question. */
		QUESTION_MODIFIED,
		/** Pour les relecteurs : nouvelle question à relire. */
		QUESTION_TO_REVIEW,
		/** Pour les relecteurs : question modifiée par son auteur, à relire de nouveau. */
		QUESTION_RESUBMITTED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipient_id")
	private User recipient;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id")
	private Question question;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Type type;

	/** Pseudo de la personne à l'origine de l'action, figé au moment de l'envoi. */
	@Column(name = "actor_name", length = 255)
	private String actorName;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "read_at")
	private Instant readAt;

	protected Notification() {
		// JPA only
	}

	public Notification(User recipient, Question question, Type type, String actorName) {
		this.recipient = recipient;
		this.question = question;
		this.type = type;
		this.actorName = actorName;
		this.createdAt = Instant.now();
	}

	public void markRead() {
		if (readAt == null) {
			readAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public User getRecipient() {
		return recipient;
	}

	public Question getQuestion() {
		return question;
	}

	public Type getType() {
		return type;
	}

	public String getActorName() {
		return actorName;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getReadAt() {
		return readAt;
	}
}
