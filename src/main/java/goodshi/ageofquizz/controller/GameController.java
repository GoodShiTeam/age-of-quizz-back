package goodshi.ageofquizz.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import goodshi.ageofquizz.service.GameService;

@Controller
public class GameController {

	@Autowired
	private GameService gameService;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/room/{code}/answer")
	public void handleAnswer(@DestinationVariable String code, AnswerPayload payload, Principal principal) {
		// process the answer and obtain the result
		var result = gameService.processAnswer(code, payload, principal);

		// send the result back to the answering client. If authenticated, send to the user's queue;
		// otherwise broadcast on the room topic (clients can filter by result.userId)
		if (principal != null && principal.getName() != null) {
			messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/room/" + code + "/answer/result", result);
		} else {
			messagingTemplate.convertAndSend("/topic/room/" + code + "/answer/result", result);
		}
	}

}
