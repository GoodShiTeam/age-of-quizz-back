package goodshi.ageofquizz.entity;

import java.util.ArrayList;
import java.util.List;

import goodshi.ageofquizz.model.DailyRiddleData.TargetType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Une réponse possible de l'énigme journalière (civilisation ou unité) avec
 * ses indices ordonnés du plus vague au plus précis.
 */
@Entity
@Table(name = "daily_riddle_entry", uniqueConstraints = @UniqueConstraint(columnNames = { "type", "name" }))
public class DailyRiddleEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TargetType type;

	/** Une entrée inactive n'est plus tirée au sort mais reste proposable. */
	@Column(nullable = false)
	private boolean active = true;

	/** Indices modifiés à la main : un import peut choisir de ne pas les écraser. */
	@Column(name = "manually_edited", nullable = false)
	private boolean manuallyEdited = false;

	@OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("position ASC")
	private List<DailyRiddleHint> hints = new ArrayList<>();

	public void replaceHints(List<DailyRiddleHint> newHints) {
		hints.clear();
		for (int i = 0; i < newHints.size(); i++) {
			DailyRiddleHint hint = newHints.get(i);
			hint.setEntry(this);
			hint.setPosition(i);
			hints.add(hint);
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TargetType getType() {
		return type;
	}

	public void setType(TargetType type) {
		this.type = type;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isManuallyEdited() {
		return manuallyEdited;
	}

	public void setManuallyEdited(boolean manuallyEdited) {
		this.manuallyEdited = manuallyEdited;
	}

	public List<DailyRiddleHint> getHints() {
		return hints;
	}
}
