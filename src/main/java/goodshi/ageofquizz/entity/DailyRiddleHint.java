package goodshi.ageofquizz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "daily_riddle_hint")
public class DailyRiddleHint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "entry_id")
	private DailyRiddleEntry entry;

	@Column(name = "hint_order", nullable = false)
	private int position;

	@Column(nullable = false, length = 100)
	private String label;

	@Column(name = "hint_text", nullable = false, length = 1000)
	private String text;

	public DailyRiddleHint() {
	}

	public DailyRiddleHint(String label, String text) {
		this.label = label;
		this.text = text;
	}

	public Integer getId() {
		return id;
	}

	public DailyRiddleEntry getEntry() {
		return entry;
	}

	public void setEntry(DailyRiddleEntry entry) {
		this.entry = entry;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
