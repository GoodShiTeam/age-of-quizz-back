package goodshi.ageofquizz.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goodshi.ageofquizz.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	/** Notifications les plus récentes d'un utilisateur, question chargée pour le libellé. */
	@Query("""
			    SELECT n FROM Notification n
			    JOIN FETCH n.question
			    WHERE n.recipient.id = :recipientId
			    ORDER BY n.createdAt DESC, n.id DESC
			""")
	List<Notification> findLatestForRecipient(@Param("recipientId") Long recipientId, Pageable pageable);

	long countByRecipientIdAndReadAtIsNull(Long recipientId);

	Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

	@Modifying
	@Query("UPDATE Notification n SET n.readAt = :now WHERE n.recipient.id = :recipientId AND n.readAt IS NULL")
	int markAllRead(@Param("recipientId") Long recipientId, @Param("now") Instant now);

	/** Une question validée/rejetée n'est plus « à relire » : on solde ces notifications. */
	@Modifying
	@Query("""
			    UPDATE Notification n SET n.readAt = :now
			    WHERE n.question.id = :questionId AND n.readAt IS NULL AND n.type IN :types
			""")
	int markReadForQuestion(@Param("questionId") Integer questionId,
			@Param("types") List<Notification.Type> types, @Param("now") Instant now);
}
