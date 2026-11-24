package goodshi.ageofquizz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.AdventAnswer;
import goodshi.ageofquizz.entity.User;
import jakarta.persistence.LockModeType;

public interface AdventAnswerRepository extends JpaRepository<AdventAnswer, Long> {

	List<AdventAnswer> findByUserAndYear(User user, int year);

	/**
	 * Verrouille les réponses du joueur jusqu'à la fin de la transaction : deux
	 * requêtes simultanées ne peuvent pas dépenser le même joker.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM AdventAnswer a WHERE a.user = :user AND a.year = :year")
	List<AdventAnswer> lockByUserAndYear(@Param("user") User user, @Param("year") int year);

	Optional<AdventAnswer> findByUserAndYearAndDay(User user, int year, int day);

	/** Remise à zéro des réponses de test d'un admin. */
	long deleteByUserAndYear(User user, int year);

	@Query("SELECT COALESCE(SUM(a.jokersUsed), 0) FROM AdventAnswer a WHERE a.user = :user AND a.year = :year")
	long sumJokersUsed(@Param("user") User user, @Param("year") int year);

	/**
	 * Par joueur : [user, bonnes réponses, cases ouvertes, instant de la
	 * dernière bonne réponse (null si aucune), jokers dépensés].
	 */
	@Query("""
			    SELECT a.user, SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END), COUNT(a),
			           MAX(CASE WHEN a.correct = true THEN a.answeredAt ELSE NULL END), SUM(a.jokersUsed)
			    FROM AdventAnswer a
			    WHERE a.year = :year
			    GROUP BY a.user
			""")
	List<Object[]> scoresByUser(@Param("year") int year);
}
