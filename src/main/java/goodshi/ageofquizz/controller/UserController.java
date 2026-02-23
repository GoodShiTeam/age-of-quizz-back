package goodshi.ageofquizz.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.dto.EmailDTO;
import goodshi.ageofquizz.dto.PasswordChangeRequestDTO;
import goodshi.ageofquizz.dto.ResetPasswordRequestDTO;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.entity.UserProfile;
import goodshi.ageofquizz.exception.ForbiddenException;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.exception.UserWithSameEmailExistException;
import goodshi.ageofquizz.exception.UserWithSamePseudoExistException;
import goodshi.ageofquizz.exception.UserWithSameUsernameExistException;
import goodshi.ageofquizz.service.CustomUserDetailsService;
import goodshi.ageofquizz.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> getUser(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUser(userId));
	}

	@GetMapping("/profile/{userProfileId}")
	public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userProfileId) {
		return ResponseEntity.ok(userService.getUserProfile(userProfileId));
	}

	@GetMapping("/current")
	public ResponseEntity<User> getCurrentUser() {
		return ResponseEntity.ok(getAuthenticatedUser());
	}

	@GetMapping("")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<User>> getUsers() {
		return ResponseEntity.ok(userService.getUsers());
	}

	@GetMapping("/profiles")
	public ResponseEntity<List<UserProfile>> getUsersProfiles() {
		return ResponseEntity.ok(userService.getUsersProfiles());
	}

	@PutMapping("/update-profile")
	public ResponseEntity<UserProfile> updateProfile(@RequestBody UserProfile userProfile)
			throws UserWithSamePseudoExistException {
		if (!getAuthenticatedUser().getUserProfile().getId().equals(userProfile.getId())) {
			throw new ForbiddenException("You can't update a profile of another account.");
		}
		return ResponseEntity.ok(userService.updateProfile(userProfile));
	}

	@PutMapping("/update")
	public ResponseEntity<User> updateProfile(@RequestBody User user) throws UserWithSamePseudoExistException,
			UserWithSameUsernameExistException, UserWithSameEmailExistException {
		if (!getAuthenticatedUser().getId().equals(user.getId())) {
			throw new ForbiddenException("You can't update a user of another account.");
		}
		return ResponseEntity.ok(userService.update(user));
	}

	@PutMapping("/update-password")
	public ResponseEntity<String> updatePassword(@RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO) {
		logger.info("updating password");
		userService.updatePassword(getAuthenticatedUser(), passwordChangeRequestDTO);
		return ResponseEntity.ok("Mot de passe mis à jour avec succès");
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@Valid @RequestBody EmailDTO email) {
		try {
			userService.forgotPassword(email.getEmail());
		} catch (ResourceNotFoundException e) {
		}
		return ResponseEntity.ok("Si un compte existe, un email a été envoyé");
	}

	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
		try {
			userService.resetPassword(request.getToken(), request.getNewPassword());
		} catch (ExpiredJwtException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expiré ❌");
		} catch (JwtException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token invalide ❌");
		}
		return ResponseEntity.ok("Mot de passe réinitialisé avec succès ✔️");
	}

	@GetMapping("/verify-email")
	public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
		userService.verifyEmail(token);
		return ResponseEntity.ok("Email vérifié avec succès !");
	}

	private User getAuthenticatedUser() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = customUserDetailsService.findByUsername(userDetails.getUsername());
		return user;
	}

}
