package goodshi.ageofquizz.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Énigme attribuée à un jour. Figée une fois créée : ajouter ou modifier des
 * entrées ne change jamais l'énigme d'un jour déjà commencé.
 */
@Entity
@Table(name = "daily_riddle_schedule")
public class DailyRiddleSchedule {

	@Id
	@Column(name = "riddle_date")
	private LocalDate date;

	@ManyToOne(optional = false)
	@JoinColumn(name = "entry_id")
	private DailyRiddleEntry entry;

	public DailyRiddleSchedule() {
	}

	public DailyRiddleSchedule(LocalDate date, DailyRiddleEntry entry) {
		this.date = date;
		this.entry = entry;
	}

	public LocalDate getDate() {
		return date;
	}

	public DailyRiddleEntry getEntry() {
		return entry;
	}
}
