package goodshi.ageofquizz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goodshi.ageofquizz.entity.GameRecapSnapshotEntity;

public interface GameRecapSnapshotRepository extends JpaRepository<GameRecapSnapshotEntity, String> {

}
