package goodshi.ageofquizz.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.Question;
import goodshi.ageofquizz.entity.Question.QuestionStatus;

public interface QuestionRepository extends JpaRepository<Question, Integer>, JpaSpecificationExecutor<Question> {

	@Query("""
			    SELECT q FROM Question q
			    WHERE q.status = :status
			    AND (:excludedIds IS NULL OR q.id NOT IN :excludedIds)
			    ORDER BY function('RAND')
			""")
	List<Question> findRandomQuestions(@Param("status") QuestionStatus status,
			@Param("excludedIds") List<Integer> excludedIds, Pageable pageable);

	@Query("""
			    SELECT DISTINCT q FROM Question q
			    LEFT JOIN FETCH q.answers
			    WHERE q.id IN :ids
			""")
	List<Question> findByIdsWithAnswers(@Param("ids") List<Integer> ids);

	/**
	 * Toutes les questions avec réponses et auteur en une requête (au lieu d'une
	 * requête par question pour charger ses réponses).
	 */
	@Query("""
			    SELECT DISTINCT q FROM Question q
			    LEFT JOIN FETCH q.answers
			    LEFT JOIN FETCH q.author
			""")
	List<Question> findAllWithAnswersAndAuthor();

	/** Pseudos distincts des auteurs ayant des questions dans ce statut. */
	@Query("""
			    SELECT DISTINCT q.author.username FROM Question q
			    WHERE q.status = :status
			    ORDER BY q.author.username
			""")
	List<String> findAuthorUsernamesByStatus(@Param("status") QuestionStatus status);

	/** Triplets [thème, civilisation, type] des questions d'un auteur dans ce statut. */
	@Query("""
			    SELECT q.theme, q.civilisation, q.type FROM Question q
			    WHERE q.author.id = :authorId AND q.status = :status
			""")
	List<Object[]> findThemeCivilisationTypeByAuthorAndStatus(@Param("authorId") Long authorId,
			@Param("status") QuestionStatus status);

	/**
	 * Par auteur : [authorId, questions créées, questions dans le statut donné
	 * (validées pour le classement)].
	 */
	@Query("""
			    SELECT q.author.id, COUNT(q), SUM(CASE WHEN q.status = :status THEN 1 ELSE 0 END)
			    FROM Question q
			    WHERE q.author IS NOT NULL
			    GROUP BY q.author.id
			""")
	List<Object[]> countByAuthorWithStatus(@Param("status") QuestionStatus status);

	/** Paires [statut, nombre] des questions créées par un auteur. */
	@Query("""
			    SELECT q.status, COUNT(q) FROM Question q
			    WHERE q.author.id = :authorId
			    GROUP BY q.status
			""")
	List<Object[]> countByAuthorGroupByStatus(@Param("authorId") Long authorId);

}
