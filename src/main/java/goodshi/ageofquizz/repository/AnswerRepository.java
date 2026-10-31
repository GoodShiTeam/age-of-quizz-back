package goodshi.ageofquizz.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {

	/** Paires [questionId, answerId] des bonnes réponses des questions données. */
	@Query("""
			    SELECT a.question.id, a.id FROM Answer a
			    WHERE a.correct = true AND a.question.id IN :questionIds
			""")
	List<Object[]> findCorrectAnswerIds(@Param("questionIds") Collection<Integer> questionIds);

	/** Paires [questionId, answerId] de toutes les bonnes réponses. */
	@Query("SELECT a.question.id, a.id FROM Answer a WHERE a.correct = true")
	List<Object[]> findAllCorrectAnswerIds();
}
