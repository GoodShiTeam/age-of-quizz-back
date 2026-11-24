package goodshi.ageofquizz.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Réponse d'un joueur à une case du calendrier de l'avent (un seul essai par
 * case). Sans lien avec user_answer : n'entre ni dans les stats de profil ni
 * dans le classement général.
 */
@Entity
@Table(name = "advent_answer", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "event_year",
		"day" }))
public class AdventAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	/** Année de l'édition, pour pouvoir refaire un calendrier l'an prochain. */
	@Column(name = "event_year", nullable = false)
	private int year;

	@Column(nullable = false)
	private int day;

	@Column(nullable = false, length = 500)
	private String answer;

	@Column(nullable = false)
	private boolean correct;

	@Column(name = "answered_at", nullable = false)
	private Instant answeredAt;

	/** Jokers dépensés pour rejouer cette case après une erreur. */
	@Column(name = "jokers_used", nullable = false)
	private int jokersUsed;

	/**
	 * Bonne réponse affichée au joueur après une erreur : la case ne peut
	 * plus être rejouée avec un joker.
	 */
	@Column(nullable = false)
	private boolean revealed;

	/**
	 * Essais gratuits utilisés (au moins 1). Seul le format ZOOM en offre
	 * plusieurs ; les essais payés par un joker n'y comptent pas.
	 */
	@Column(nullable = false)
	private int attempts = 1;

	/**
	 * Question de quiz posée dans une case « rattrapage » (tirée parmi les
	 * questions ratées du joueur), null pour les autres cases. Lien en lecture
	 * seule : la réponse n'est jamais enregistrée dans user_answer.
	 */
	@Column(name = "quiz_question_id")
	private Integer quizQuestionId;

	protected AdventAnswer() {
		// JPA only
	}

	public AdventAnswer(User user, int year, int day, String answer, boolean correct, Integer quizQuestionId) {
		this.user = user;
		this.year = year;
		this.day = day;
		this.answer = answer;
		this.correct = correct;
		this.quizQuestionId = quizQuestionId;
		this.answeredAt = Instant.now();
	}

	public Integer getQuizQuestionId() {
		return quizQuestionId;
	}

	/** Nouvel essai payé par un joker : remplace la réponse précédente. */
	public void retryWithJoker(String newAnswer, boolean newCorrect) {
		this.answer = newAnswer;
		this.correct = newCorrect;
		this.jokersUsed++;
		this.answeredAt = Instant.now();
	}

	/** Nouvel essai gratuit (format ZOOM) : remplace la réponse précédente. */
	public void retryFree(String newAnswer, boolean newCorrect) {
		this.answer = newAnswer;
		this.correct = newCorrect;
		this.attempts++;
		this.answeredAt = Instant.now();
	}

	public int getAttempts() {
		return attempts;
	}

	public int getJokersUsed() {
		return jokersUsed;
	}

	public void reveal() {
		this.revealed = true;
	}

	/** Une bonne réponse est toujours affichée. */
	public boolean isRevealed() {
		return revealed || correct;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public int getYear() {
		return year;
	}

	public int getDay() {
		return day;
	}

	public String getAnswer() {
		return answer;
	}

	public boolean isCorrect() {
		return correct;
	}

	public Instant getAnsweredAt() {
		return answeredAt;
	}
}
