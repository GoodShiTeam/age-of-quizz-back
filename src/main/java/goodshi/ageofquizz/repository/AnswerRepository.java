package goodshi.ageofquizz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goodshi.ageofquizz.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
}
