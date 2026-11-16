package goodshi.ageofquizz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.UserAnswer;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Integer> {

	List<UserAnswer> findByUserIdAndQuestionIdOrderByAnsweredAtDesc(Integer userId, Integer questionId);

	Optional<UserAnswer> findFirstByUserIdAndQuestionIdOrderByAnsweredAtDesc(Integer userId, Integer questionId);

	/** Toutes les réponses d'un joueur avec question et réponse, pour ses stats. */
	@Query("""
			    SELECT ua FROM UserAnswer ua
			    JOIN FETCH ua.question
			    JOIN FETCH ua.answer
			    WHERE ua.user.id = :userId
			""")
	List<UserAnswer> findAllWithQuestionByUserId(@Param("userId") Long userId);

	/** Réponses données par les autres joueurs aux questions d'un auteur. */
	@Query("""
			    SELECT COUNT(ua) FROM UserAnswer ua
			    WHERE ua.question.author.id = :authorId AND ua.user.id <> :authorId
			""")
	long countAnswersToAuthorQuestions(@Param("authorId") Long authorId);

	/**
	 * Toutes les réponses de tous les joueurs, pour le classement :
	 * [userId, questionId, answerId, answeredAt, responseTimeSeconds].
	 */
	@Query("""
			    SELECT ua.user.id, ua.question.id, ua.answer.id, ua.answeredAt, ua.responseTimeSeconds
			    FROM UserAnswer ua
			""")
	List<Object[]> findAllAnswerRows();
}