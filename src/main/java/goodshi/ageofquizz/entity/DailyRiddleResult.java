package goodshi.ageofquizz.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import goodshi.ageofquizz.entity.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import jakarta.persistence.UniqueConstraint;

/** Partie d'énigme journalière d'un utilisateur connecté (une par jour). */
@Entity
@Table(name = "daily_riddle_result", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
		"riddle_date" }))
public class DailyRiddleResult {

	public enum Status {
		PLAYING, WON, LOST
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "riddle_date", nullable = false)
	private LocalDate date;

	/** Propositions dans l'ordre, la dernière étant la bonne si la partie est gagnée. */
	@Convert(converter = StringListConverter.class)
	@Column(nullable = false, length = 2000)
	private List<String> guesses = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Status status = Status.PLAYING;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public DailyRiddleResult() {
	}

	public DailyRiddleResult(User user, LocalDate date) {
		this.user = user;
		this.date = date;
		this.updatedAt = Instant.now();
	}

	public void addGuess(String guess, Status newStatus) {
		guesses.add(guess);
		status = newStatus;
		updatedAt = Instant.now();
	}

	public boolean isFinished() {
		return status != Status.PLAYING;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public LocalDate getDate() {
		return date;
	}

	public List<String> getGuesses() {
		return guesses;
	}

	public Status getStatus() {
		return status;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
