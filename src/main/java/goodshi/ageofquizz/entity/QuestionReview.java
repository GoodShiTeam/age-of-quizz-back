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
 * Historique des actions de relecture (validation, rejet, modification d'une
 * question d'un autre auteur). Sert aux statistiques et badges des relecteurs.
 */
@Entity
@Table(name = "question_review")
public class QuestionReview {

	public enum Action {
		VALIDATED, REJECTED, MODIFIED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reviewer_id")
	private User reviewer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id")
	private Question question;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Action action;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected QuestionReview() {
		// JPA only
	}

	public QuestionReview(User reviewer, Question question, Action action) {
		this.reviewer = reviewer;
		this.question = question;
		this.action = action;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public User getReviewer() {
		return reviewer;
	}

	public Question getQuestion() {
		return question;
	}

	public Action getAction() {
		return action;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
