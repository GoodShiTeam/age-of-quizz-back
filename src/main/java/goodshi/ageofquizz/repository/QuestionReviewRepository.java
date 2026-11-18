package goodshi.ageofquizz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.QuestionReview;

public interface QuestionReviewRepository extends JpaRepository<QuestionReview, Long> {

	/** Paires [action, nombre] des actions d'un relecteur. */
	@Query("""
			    SELECT r.action, COUNT(r) FROM QuestionReview r
			    WHERE r.reviewer.id = :reviewerId
			    GROUP BY r.action
			""")
	List<Object[]> countByReviewerGroupByAction(@Param("reviewerId") Long reviewerId);

	/** Auteurs distincts (hors le relecteur lui-même) dont il a relu des questions. */
	@Query("""
			    SELECT COUNT(DISTINCT r.question.author.id) FROM QuestionReview r
			    WHERE r.reviewer.id = :reviewerId AND r.question.author.id <> :reviewerId
			""")
	long countDistinctAuthorsReviewed(@Param("reviewerId") Long reviewerId);
}
