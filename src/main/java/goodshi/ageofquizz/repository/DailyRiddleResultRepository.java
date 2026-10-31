package goodshi.ageofquizz.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import goodshi.ageofquizz.entity.DailyRiddleResult;
import goodshi.ageofquizz.entity.User;

public interface DailyRiddleResultRepository extends JpaRepository<DailyRiddleResult, Long> {

	Optional<DailyRiddleResult> findByUserAndDate(User user, LocalDate date);

	List<DailyRiddleResult> findByUserOrderByDateAsc(User user);

	/** Joueurs ayant au moins une énigme enregistrée, pour le classement. */
	@Query("SELECT DISTINCT r.user FROM DailyRiddleResult r")
	List<User> findDistinctUsers();
}
