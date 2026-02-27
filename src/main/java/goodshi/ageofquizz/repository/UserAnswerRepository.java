package goodshi.ageofquizz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goodshi.ageofquizz.entity.UserAnswer;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Integer> {

	List<UserAnswer> findByUserIdAndQuestionIdOrderByAnsweredAtDesc(Integer userId, Integer questionId);

	Optional<UserAnswer> findFirstByUserIdAndQuestionIdOrderByAnsweredAtDesc(Integer userId, Integer questionId);
}