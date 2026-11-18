package goodshi.ageofquizz.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.QuestionReport;
import goodshi.ageofquizz.entity.User;

public interface QuestionReportRepository extends JpaRepository<QuestionReport, Long> {

	/** Signalement encore ouvert d'un joueur sur une question (un seul à la fois). */
	Optional<QuestionReport> findByQuestionIdAndReporterIdAndResolvedAtIsNull(Integer questionId, Long reporterId);

	/** Tous les signalements ouverts, signaleur chargé pour son pseudo. */
	@Query("""
			    SELECT r FROM QuestionReport r
			    JOIN FETCH r.reporter rep
			    LEFT JOIN FETCH rep.userProfile
			    WHERE r.resolvedAt IS NULL
			    ORDER BY r.createdAt DESC
			""")
	List<QuestionReport> findAllOpen();

	@Modifying
	@Query("""
			    UPDATE QuestionReport r SET r.resolvedAt = :now, r.resolvedBy = :reviewer
			    WHERE r.question.id = :questionId AND r.resolvedAt IS NULL
			""")
	int resolveAllForQuestion(@Param("questionId") Integer questionId, @Param("reviewer") User reviewer,
			@Param("now") Instant now);
}
