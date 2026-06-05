package goodshi.ageofquizz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goodshi.ageofquizz.entity.GameSessionEntity;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {

}
