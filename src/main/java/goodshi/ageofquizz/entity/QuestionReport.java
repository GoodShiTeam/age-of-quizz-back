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
 * Signalement d'une question par un joueur (question plus à jour, réponse
 * fausse...). Reste ouvert jusqu'à ce qu'un relecteur le marque comme traité.
 */
@Entity
@Table(name = "question_report")
public class QuestionReport {

	public enum Reason {
		/** Plus à jour (patch, équilibrage...). */
		OUTDATED,
		/** La bonne réponse indiquée est fausse. */
		WRONG_ANSWER,
		/** Faute d'orthographe, formulation ambiguë. */
		WORDING,
		/** Image ou son absent, illisible ou ne correspondant pas. */
		MEDIA,
		/** Autre : le commentaire est obligatoire. */
		OTHER
	}

	public static final int COMMENT_MAX_LENGTH = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id")
	private Question question;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reporter_id")
	private User reporter;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Reason reason;

	@Column(length = COMMENT_MAX_LENGTH)
	private String comment;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resolved_by_id")
	private User resolvedBy;

	protected QuestionReport() {
		// JPA only
	}

	public QuestionReport(Question question, User reporter, Reason reason, String comment) {
		this.question = question;
		this.reporter = reporter;
		this.createdAt = Instant.now();
		update(reason, comment);
	}

	/** Nouveau signalement du même joueur sur une question déjà signalée par lui. */
	public void update(Reason reason, String comment) {
		this.reason = reason;
		this.comment = comment;
	}

	public void resolve(User reviewer) {
		if (resolvedAt == null) {
			resolvedAt = Instant.now();
			resolvedBy = reviewer;
		}
	}

	public Long getId() {
		return id;
	}

	public Question getQuestion() {
		return question;
	}

	public User getReporter() {
		return reporter;
	}

	public Reason getReason() {
		return reason;
	}

	public String getComment() {
		return comment;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getResolvedAt() {
		return resolvedAt;
	}

	public User getResolvedBy() {
		return resolvedBy;
	}
}
