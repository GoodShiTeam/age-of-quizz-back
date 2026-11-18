package goodshi.ageofquizz.dto;

import java.time.Instant;

import goodshi.ageofquizz.entity.Notification;

public record NotificationDTO(Long id, Notification.Type type, Integer questionId, String questionLibelle,
		String actorName, Instant createdAt, boolean read) {

	public static NotificationDTO fromEntity(Notification n) {
		return new NotificationDTO(n.getId(), n.getType(), n.getQuestion().getId(), n.getQuestion().getLibelle(),
				n.getActorName(), n.getCreatedAt(), n.getReadAt() != null);
	}
}
