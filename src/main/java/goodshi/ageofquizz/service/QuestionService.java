package goodshi.ageofquizz.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.dto.AnswerCreateRequestDTO;
import goodshi.ageofquizz.dto.FilterQuestionDTO;
import goodshi.ageofquizz.dto.QuestionCreateRequestDTO;
import goodshi.ageofquizz.dto.QuestionDTO;
import goodshi.ageofquizz.entity.Answer;
import goodshi.ageofquizz.entity.Notification;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.QuestionReview;
import goodshi.ageofquizz.entity.Question.QuestionStatus;
import goodshi.ageofquizz.entity.Question.QuestionTheme;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.enums.RoleEnum;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.repository.QuestionRepository;
import goodshi.ageofquizz.repository.QuestionReviewRepository;
import goodshi.ageofquizz.util.QuestionSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class QuestionService {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private QuestionReviewRepository questionReviewRepository;

	@Transactional
	public Question createQuestion(QuestionCreateRequestDTO request, User author) {

		Question question = new Question(request.getTheme(), request.getLibelle(), request.getType(), author);

		Question createdQuestion = questionRepository.save(buildQuestion(question, request, author));
		notificationService.notifyReviewers(createdQuestion, author, false);
		return createdQuestion;
	}

	private Question buildQuestion(Question question, QuestionCreateRequestDTO request, User author) {
		question.setFileUrl(request.getFileUrl());
		question.setCivilisation(request.getCivilisation());
		question.setBuilding(request.getBuilding());
		question.setExplication(request.getExplication());

		for (AnswerCreateRequestDTO answerRequest : request.getAnswers()) {
			Answer answer = new Answer(answerRequest.getValue(), answerRequest.isCorrect());
			question.addAnswer(answer);
		}
		return question;
	}

	@Transactional
	public List<QuestionDTO> getAllQuestions() {
		return questionRepository.findAllWithAnswersAndAuthor().stream().map(QuestionDTO::fromEntity).toList();
	}

	@Transactional
	public Question updateStatusQuestion(Integer id, QuestionStatus status, User reviewer) {
		Question question = questionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Question id : " + id + " doesn't exist"));

		// Un relecteur ne tranche pas sur ses propres questions (il peut les modifier) ; l'admin si.
		boolean isAdmin = reviewer.getRoles().stream().anyMatch(role -> role.getName() == RoleEnum.ADMIN);
		boolean isOwner = question.getAuthor() != null
				&& Objects.equals(question.getAuthor().getId(), reviewer.getId());
		if (isOwner && !isAdmin) {
			throw new AccessDeniedException("Vous ne pouvez pas valider ou rejeter vos propres questions");
		}

		QuestionStatus previous = question.getStatus();
		question.setStatus(status);
		Question saved = questionRepository.save(question);

		// Pas de notification si le statut ne change pas (double clic, rejeu).
		if (previous != status && (status == QuestionStatus.VALIDATED || status == QuestionStatus.REJECTED)) {
			notificationService.notifyAuthor(saved, reviewer, status == QuestionStatus.VALIDATED
					? Notification.Type.QUESTION_VALIDATED
					: Notification.Type.QUESTION_REJECTED);
			notificationService.clearReviewRequests(saved);
			questionReviewRepository.save(new QuestionReview(reviewer, saved,
					status == QuestionStatus.VALIDATED ? QuestionReview.Action.VALIDATED
							: QuestionReview.Action.REJECTED));
		}
		return saved;
	}

	@Transactional
	public Question updateQuestion(QuestionCreateRequestDTO request, User author) {
		Question question = questionRepository.findById(request.getId()).orElseThrow(
				() -> new ResourceNotFoundException("Question with id : " + request.getId() + " doesn't exist"));

		// Un auteur ne modifie que ses propres questions, un relecteur ou un admin toutes.
		boolean isAdminOrReviewer = author.getRoles().stream()
				.anyMatch(role -> role.getName() == RoleEnum.ADMIN || role.getName() == RoleEnum.REVIEWER);
		boolean isOwner = question.getAuthor() != null && Objects.equals(question.getAuthor().getId(), author.getId());
		if (!isAdminOrReviewer && !isOwner) {
			throw new AccessDeniedException("Vous ne pouvez modifier que vos propres questions");
		}

		question.getAnswers().clear();

		Question updatedQuestion = buildQuestion(question, request, author);
		updatedQuestion.setTheme(request.getTheme());
		updatedQuestion.setLibelle(request.getLibelle());
		updatedQuestion.setType(request.getType());
		updatedQuestion.setStatus(QuestionStatus.CREATED_REVIEW);

		Question saved = questionRepository.save(updatedQuestion);
		if (isOwner) {
			// L'auteur a corrigé sa question : elle repart en relecture.
			notificationService.notifyReviewers(saved, author, true);
		} else {
			notificationService.notifyAuthor(saved, author, Notification.Type.QUESTION_MODIFIED);
			// Correction de la question d'un autre : action de relecture.
			questionReviewRepository.save(new QuestionReview(author, saved, QuestionReview.Action.MODIFIED));
		}
		return saved;
	}

	public List<QuestionDTO> getQuestions(@Valid FilterQuestionDTO filter) {
		return selectQuestions(filter).stream().map(QuestionDTO::fromEntity).toList();
	}

	/** Pseudos des auteurs ayant au moins une question validée, triés. */
	public List<String> getAuthorsWithValidatedQuestions() {
		return questionRepository.findAuthorUsernamesByStatus(QuestionStatus.VALIDATED);
	}

	/**
	 * Tire les questions validées correspondant au filtre, complétées par
	 * d'autres questions s'il n'y en a pas assez. Le tirage est équilibré entre
	 * les styles de questions (voir {@link #balancedDraw}) plutôt que
	 * proportionnel au contenu de la base.
	 */
	public List<Question> selectQuestions(FilterQuestionDTO filter) {

		int wanted = filter.getNumberOfQuestions();
		List<QuestionTheme> excludedThemes = filter.getExcludedThemes() == null
				|| filter.getExcludedThemes().isEmpty() ? null : filter.getExcludedThemes();

		Specification<Question> spec = Specification.where(QuestionSpecification.hasTheme(filter.getTheme()))
				.and(QuestionSpecification.hasNoneOfThemes(excludedThemes))
				.and(QuestionSpecification.hasCivilisation(filter.getCivilisation()))
				.and(QuestionSpecification.hasAuthor(filter.getAuthorUsername()))
				.and(QuestionSpecification.isStatus(QuestionStatus.VALIDATED))
				.and(QuestionSpecification.hasBuilding(filter.getBuilding()));

		List<Question> result = new ArrayList<>(balancedDraw(questionRepository.findAll(spec), wanted));

		int missing = wanted - result.size();
		if (missing > 0) {
			// Le complément respecte aussi les thèmes écartés.
			Set<Integer> takenIds = result.stream().map(Question::getId).collect(Collectors.toSet());
			List<Question> pool = questionRepository.findAll(Specification
					.where(QuestionSpecification.isStatus(QuestionStatus.VALIDATED))
					.and(QuestionSpecification.hasNoneOfThemes(excludedThemes)));
			pool.removeIf(q -> takenIds.contains(q.getId()));
			result.addAll(balancedDraw(pool, missing));
		}

		Collections.shuffle(result);
		return result;
	}

	/**
	 * Tire jusqu'à {@code count} questions en équilibrant les thèmes (arbre des
	 * technologies, image, son...) : chaque thème donne une question à tour de
	 * rôle, puis, dans un thème, les types (QCM, vrai/faux...) alternent. Un
	 * style épuisé laisse sa place aux autres. Sans ça, un thème très fourni en
	 * base remplirait presque toute la partie.
	 */
	static List<Question> balancedDraw(List<Question> pool, int count) {
		// Les questions sans thème forment leur propre groupe ("")
		Map<String, List<Question>> byTheme = pool.stream()
				.collect(Collectors.groupingBy(q -> Optional.ofNullable(q.getTheme()).map(Enum::name).orElse("")));
		List<Deque<Question>> themes = byTheme.values().stream()
				.map(questions -> (Deque<Question>) new ArrayDeque<>(interleaveTypes(questions)))
				.collect(Collectors.toCollection(ArrayList::new));
		// Ordre des thèmes tiré au hasard : avec peu de questions demandées, ce ne
		// sont pas toujours les mêmes thèmes qui passent en premier
		Collections.shuffle(themes);

		List<Question> drawn = new ArrayList<>();
		while (drawn.size() < count && !themes.isEmpty()) {
			for (Iterator<Deque<Question>> it = themes.iterator(); it.hasNext() && drawn.size() < count;) {
				Deque<Question> theme = it.next();
				drawn.add(theme.poll());
				if (theme.isEmpty()) {
					it.remove();
				}
			}
		}
		return drawn;
	}

	/** Questions d'un thème, mélangées, en alternant les types. */
	private static List<Question> interleaveTypes(List<Question> questions) {
		List<Deque<Question>> types = questions.stream()
				.collect(Collectors.groupingBy(q -> Optional.ofNullable(q.getType()).map(Enum::name).orElse("")))
				.values().stream().map(list -> {
					List<Question> shuffled = new ArrayList<>(list);
					Collections.shuffle(shuffled);
					return (Deque<Question>) new ArrayDeque<>(shuffled);
				}).collect(Collectors.toCollection(ArrayList::new));
		Collections.shuffle(types);

		List<Question> interleaved = new ArrayList<>();
		while (!types.isEmpty()) {
			for (Iterator<Deque<Question>> it = types.iterator(); it.hasNext();) {
				Deque<Question> type = it.next();
				interleaved.add(type.poll());
				if (type.isEmpty()) {
					it.remove();
				}
			}
		}
		return interleaved;
	}
}
