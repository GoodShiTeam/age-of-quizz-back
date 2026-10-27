package goodshi.ageofquizz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import goodshi.ageofquizz.entity.DailyRiddleEntry;
import goodshi.ageofquizz.model.DailyRiddleData.TargetType;

public interface DailyRiddleEntryRepository extends JpaRepository<DailyRiddleEntry, Integer> {

	List<DailyRiddleEntry> findByType(TargetType type);

	List<DailyRiddleEntry> findByTypeOrderByNameAsc(TargetType type);

	Optional<DailyRiddleEntry> findByTypeAndName(TargetType type, String name);

	@Query("""
			select e from DailyRiddleEntry e
			where e.type = :type and e.active = true
			and not exists (select s from DailyRiddleSchedule s where s.entry = e)
			""")
	List<DailyRiddleEntry> findActiveNeverScheduled(TargetType type);

	List<DailyRiddleEntry> findByTypeAndActiveTrue(TargetType type);
}
