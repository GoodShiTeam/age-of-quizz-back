package goodshi.ageofquizz.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.GamePlayerResultEntity;

public interface GamePlayerResultRepository extends JpaRepository<GamePlayerResultEntity, Integer> {

	List<GamePlayerResultEntity> findByUserId(Long userId);

	/** Paires [gameSessionId, nombre de joueurs] des parties données. */
	@Query("""
			    SELECT r.gameSessionId, COUNT(r) FROM GamePlayerResultEntity r
			    WHERE r.gameSessionId IN :sessionIds
			    GROUP BY r.gameSessionId
			""")
	List<Object[]> countPlayersBySession(@Param("sessionIds") Collection<String> sessionIds);

	/** Paires [gameSessionId, nombre de joueurs] de toutes les parties. */
	@Query("SELECT r.gameSessionId, COUNT(r) FROM GamePlayerResultEntity r GROUP BY r.gameSessionId")
	List<Object[]> countPlayersByAllSessions();
}
