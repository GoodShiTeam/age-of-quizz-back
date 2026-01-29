package goodshi.ageofquizz.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "question")
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(name = "theme")
	private QuestionTheme theme;

	@Enumerated(EnumType.STRING)
	@Column(name = "civilisation")
	private QuestionCivilisation civilisation;

	@Enumerated(EnumType.STRING)
	@Column(name = "building")
	private QuestionBuilding building;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private QuestionStatus status;

	@Column(name = "libelle", columnDefinition = "TEXT")
	private String libelle;

	@Column(name = "file_url", columnDefinition = "TEXT")
	private String fileUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private QuestionType type;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Answer> answers = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "author", referencedColumnName = "id")
	private User author;

	/*
	 * ======================= Constructeurs =======================
	 */

	protected Question() {
		// JPA only
	}

	public Question(QuestionTheme theme, String libelle, QuestionType type, User author) {
		this.theme = theme;
		this.libelle = libelle;
		this.type = type;
		this.author = author;
		this.status = QuestionStatus.CREATED_REVIEW;
	}

	/*
	 * ======================= Enums =======================
	 */

	public enum QuestionTheme {
		TECH_TREE, IMAGE, UNIT_STATS, SOUND
	}

	public enum QuestionType {
		MULTIPLE, TRUE_FALSE, SOUND, IMAGE
	}

	public enum QuestionStatus {
		CREATED_REVIEW, CLAIMED_REVIEW, VALIDATED, REJECTED
	}

	public enum QuestionCivilisation {
		NONE, ARMENIANS, AZTECS, BENGALIS, BERBERS, BOHEMIANS, BRITONS, BULGARIANS, BURGUNDIANS, BURMESE, BYZANTINES,
		CELTS, CHINESE, CUMANS, DRAVIDIANS, ETHIOPIANS, FRANKS, GEORGIANS, GOTHS, GURJARAS, HINDUSTANIS, HUNS, INCAS,
		ITALIANS, JAPANESE, JURCHENS, KHITANS, KHMER, KOREANS, LITHUANIANS, MAGYARS, MALAY, MALIANS, MAYANS, MONGOLS,
		PERSIANS, POLES, PORTUGUESE, ROMANS, SARACENS, SHU, SICILIANS, SLAVS, SPANISH, TATARS, TEUTONS, TURKS,
		VIETNAMESE, VIKINGS, WEI, WU
	}

	public enum QuestionBuilding {

		TOWN_CENTER, MARKET, LUMBER_CAMP, MINING_CAMP, DOCK, BLACKSMITH, UNIVERSITY, BARRACKS, ARCHERY_RANGE, STABLE,
		SIEGE_WORKSHOP, MONASTERY, CASTLE, TOWER

	}

	/*
	 * ======================= Méthodes utilitaires =======================
	 */

	public void addAnswer(Answer answer) {
		answers.add(answer);
		answer.setQuestion(this);
	}

	public void removeAnswer(Answer answer) {
		answers.remove(answer);
		answer.setQuestion(null);
	}

	/*
	 * ======================= Getters / Setters =======================
	 */

	public Integer getId() {
		return id;
	}

	public QuestionTheme getTheme() {
		return theme;
	}

	public void setTheme(QuestionTheme theme) {
		this.theme = theme;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public QuestionType getType() {
		return type;
	}

	public void setType(QuestionType type) {
		this.type = type;
	}

	public List<Answer> getAnswers() {
		return answers;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public QuestionCivilisation getCivilisation() {
		return civilisation;
	}

	public void setCivilisation(QuestionCivilisation civilisation) {
		this.civilisation = civilisation;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public QuestionStatus getStatus() {
		return status;
	}

	public void setStatus(QuestionStatus status) {
		this.status = status;
	}

	public QuestionBuilding getBuilding() {
		return building;
	}

	public void setBuilding(QuestionBuilding building) {
		this.building = building;
	}

}
