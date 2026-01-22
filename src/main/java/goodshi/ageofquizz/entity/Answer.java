package goodshi.ageofquizz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "answer")
public class Answer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Column(name = "value", columnDefinition = "TEXT", nullable = false)
	private String value;

	@Column(name = "correct")
	private Boolean correct = false;

	/*
	 * ======================= Constructeurs =======================
	 */

	protected Answer() {
		// JPA only
	}

	public Answer(String value, Boolean isCorrect) {
		this.value = value;
		this.correct = isCorrect;
	}

	/*
	 * ======================= Getters / Setters =======================
	 */

	public Integer getId() {
		return id;
	}

	public Question getQuestion() {
		return question;
	}

	void setQuestion(Question question) {
		this.question = question;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean isCorrect() {
		return correct;
	}

	public void setCorrect(Boolean correct) {
		this.correct = correct;
	}
}
