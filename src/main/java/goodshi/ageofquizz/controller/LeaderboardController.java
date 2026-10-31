package goodshi.ageofquizz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.dto.LeaderboardDTO;
import goodshi.ageofquizz.service.LeaderboardService;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

	@Autowired
	private LeaderboardService leaderboardService;

	@GetMapping
	public ResponseEntity<LeaderboardDTO> getLeaderboard() {
		return ResponseEntity.ok(leaderboardService.getLeaderboard());
	}
}
