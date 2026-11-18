package goodshi.ageofquizz.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import goodshi.ageofquizz.dto.QuestionReportDTO;
import goodshi.ageofquizz.dto.QuestionReportRequestDTO;
import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.QuestionReport;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.repository.QuestionReportRepository;
import goodshi.ageofquizz.repository.QuestionRepository;
import jakarta.transaction.Transactional;

/**
 * Signalements de questions par les joueurs. Un joueur n'a qu'un signalement
 * ouvert par question : le renvoyer met à jour le motif sans re-notifier.
 */
@Service
public class QuestionReportService {

	@Autowired
	private QuestionReportRepository questionReportRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private NotificationService notificationService;

	@Transactional
	public QuestionReportDTO report(Integer questionId, QuestionReportRequestDTO request, User reporter) {
		Question question = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException("Question id : " + questionId + " doesn't exist"));

		String comment = request.comment() == null || request.comment().isBlank() ? null
				: request.comment().trim();
		if (request.reason() == QuestionReport.Reason.OTHER && comment == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Merci de préciser le problème pour le motif « Autre »");
		}

		Optional<QuestionReport> existing = questionReportRepository
				.findByQuestionIdAndReporterIdAndResolvedAtIsNull(questionId, reporter.getId());
		if (existing.isPresent()) {
			existing.get().update(request.reason(), comment);
			return QuestionReportDTO.fromEntity(existing.get());
		}

		QuestionReport saved = questionReportRepository
				.save(new QuestionReport(question, reporter, request.reason(), comment));
		notificationService.notifyReported(question, reporter);
		return QuestionReportDTO.fromEntity(saved);
	}

	@Transactional
	public List<QuestionReportDTO> getOpenReports() {
		return questionReportRepository.findAllOpen().stream().map(QuestionReportDTO::fromEntity).toList();
	}

	/** Le relecteur a traité la question : tous ses signalements ouverts sont clos. */
	@Transactional
	public void resolveAll(Integer questionId, User reviewer) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResourceNotFoundException("Question id : " + questionId + " doesn't exist");
		}
		questionReportRepository.resolveAllForQuestion(questionId, reviewer, Instant.now());
		notificationService.clearReportNotifications(questionId);
	}
}
