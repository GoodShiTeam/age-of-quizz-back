package goodshi.ageofquizz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.authentication.AuthCredentialsRequest;
import goodshi.ageofquizz.authentication.JwtUtil;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.exception.UserWithSameEmailExistException;
import goodshi.ageofquizz.exception.UserWithSamePseudoExistException;
import goodshi.ageofquizz.exception.UserWithSameUsernameExistException;
import goodshi.ageofquizz.service.AuthService;
import goodshi.ageofquizz.service.UserService;

/**
 * AuthController is a Spring MVC RestController responsible for handling
 * authentication-related HTTP requests. It defines an endpoint for user login
 * using authentication credentials.
 *
 * <p>
 * This controller uses the {@link AuthService} for user authentication and
 * issues JSON Web Tokens (JWTs) using the {@link JwtUtil}.
 * </p>
 *
 * <p>
 * The endpoint provided by this controller is:
 * </p>
 * <ul>
 * <li><strong>POST /login:</strong> Handles user login using the provided
 * authentication credentials. The request body should contain an
 * {@link AuthCredentialsRequest} representing the user credentials.</li>
 * </ul>
 *
 */
@RestController
public class AuthController {

	@Autowired
	private AuthService loginService;

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody AuthCredentialsRequest request) {
		logger.info("logging");
		try {
			UserDetails userDetails = loginService.getUserFromRequest(request);
			User user = userService.findByUsername(userDetails.getUsername());
			user.setPassword("");
			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtUtil.generateToken(user.getUsername()))
					.body(user);
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody User user) throws UserWithSamePseudoExistException,
			UserWithSameUsernameExistException, UserWithSameEmailExistException {
		logger.info("registering");
		userService.registerUser(user);
		return ResponseEntity.ok("User registered successfully");
	}

	@GetMapping("/validate-token")
	public ResponseEntity<String> validateToken(@RequestParam String token) {
		try {
			String username = jwtUtil.extractUsername(token);

			if (!jwtUtil.isTokenValid(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
			}

			return ResponseEntity.ok("Token is valid for user: " + username);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
		}
	}

}