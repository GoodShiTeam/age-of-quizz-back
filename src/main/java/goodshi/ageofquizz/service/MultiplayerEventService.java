package goodshi.ageofquizz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.model.GameEvent;

@Service
public class MultiplayerEventService {

	private final SimpMessagingTemplate messagingTemplate;

	@Autowired
	public MultiplayerEventService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	/**
	 * Send an event to all clients subscribed to the room topic.
	 */
	public void sendToRoom(String roomCode, GameEvent event) {
		String destination = "/topic/room/" + roomCode;
		messagingTemplate.convertAndSend(destination, event);
	}

}

