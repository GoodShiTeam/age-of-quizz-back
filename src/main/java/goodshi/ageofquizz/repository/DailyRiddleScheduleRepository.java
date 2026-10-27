package goodshi.ageofquizz.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goodshi.ageofquizz.entity.DailyRiddleEntry;
import goodshi.ageofquizz.entity.DailyRiddleSchedule;

public interface DailyRiddleScheduleRepository extends JpaRepository<DailyRiddleSchedule, LocalDate> {

	List<DailyRiddleSchedule> findTop30ByOrderByDateDesc();

	Optional<DailyRiddleSchedule> findFirstByEntryOrderByDateDesc(DailyRiddleEntry entry);
}
