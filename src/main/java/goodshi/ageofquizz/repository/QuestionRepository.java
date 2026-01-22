package goodshi.ageofquizz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goodshi.ageofquizz.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
