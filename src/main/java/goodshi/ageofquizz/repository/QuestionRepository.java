package goodshi.ageofquizz.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer>, JpaSpecificationExecutor<Question> {

	@Query("""
			    SELECT q FROM Question q
			    WHERE (:excludedIds IS NULL OR q.id NOT IN :excludedIds)
			    ORDER BY function('RAND')
			""")
	List<Question> findRandomQuestions(@Param("excludedIds") Collection<Integer> excludedIds, Pageable pageable);
}
