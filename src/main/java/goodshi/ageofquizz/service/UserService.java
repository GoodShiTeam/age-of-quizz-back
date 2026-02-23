package goodshi.ageofquizz.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.dto.PasswordChangeRequestDTO;
import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.entity.UserProfile;
import goodshi.ageofquizz.exception.EmailAlreadyVerifiedException;
import goodshi.ageofquizz.exception.ExpiredVerificationTokenException;
import goodshi.ageofquizz.exception.InvalidVerificationTokenException;
import goodshi.ageofquizz.exception.ResourceNotFoundException;
import goodshi.ageofquizz.exception.UserWithSameEmailExistException;
import goodshi.ageofquizz.exception.UserWithSamePseudoExistException;
import goodshi.ageofquizz.exception.UserWithSameUsernameExistException;
import goodshi.ageofquizz.repository.UserProfileRepository;
import goodshi.ageofquizz.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

	@Value("${jwt.secret}")
	private String secret;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	private UserProfileRepository userProfileRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public void registerUser(User user) throws UserWithSamePseudoExistException, UserWithSameUsernameExistException,
			UserWithSameEmailExistException {
		Assert.notNull(user, "user can't be null");
		Assert.notNull(user.getUserProfile(), "user profile can't be null");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.getUserProfile().setAvatar("defaultAvatar.png");
		checkIfPseudoAlreadyExists(user.getUserProfile());
		checkIfUsernameAlreadyExists(user);
		checkIfEmailAlreadyExists(user);
		userRepository.save(user);
		mailService.sendEmailVerification(user.getEmail(), generateVerificationEmailToken(secret));
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Profil utilisateur non trouvé"));
	}

	public User getUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Profil utilisateur non trouvé"));
	}

	public User getUserWithPseudo(String pseudo) {
		return userRepository.findByUserProfilePseudo(pseudo)
				.orElseThrow(() -> new ResourceNotFoundException("Profil utilisateur non trouvé"));
	}

	public UserProfile getUserProfile(Long userProfileId) {
		return userProfileRepository.findById(userProfileId)
				.orElseThrow(() -> new ResourceNotFoundException("Profil utilisateur non trouvé"));
	}

	public List<User> getUsers() {
		return userRepository.findAll();

	}

	public UserProfile updateProfile(UserProfile userProfile) throws UserWithSamePseudoExistException {
		checkIfPseudoAlreadyExists(userProfile);
		return userProfileRepository.save(userProfile);

	}

	private void checkIfPseudoAlreadyExists(UserProfile userProfile) throws UserWithSamePseudoExistException {
		Assert.notNull(userProfile, "Profile can't be null");
		Assert.notNull(userProfile.getPseudo(), "Pseudo can't be null");
		Optional<UserProfile> userProfileFromDB = userProfileRepository.findByPseudo(userProfile.getPseudo());
		if (userProfileFromDB.isPresent() && !userProfileFromDB.get().getId().equals(userProfile.getId())) {
			throw new UserWithSamePseudoExistException(userProfile.getPseudo());
		}
	}

	private void checkIfUsernameAlreadyExists(User user) throws UserWithSameUsernameExistException {
		Assert.notNull(user, "User can't be null");
		Assert.notNull(user.getUsername(), "Username can't be null");
		Optional<User> userFromDB = userRepository.findByUsername(user.getUsername());
		if (userFromDB.isPresent() && !userFromDB.get().getId().equals(user.getId())) {
			throw new UserWithSameUsernameExistException(user.getUsername());
		}
	}

	private void checkIfEmailAlreadyExists(User user) throws UserWithSameEmailExistException {
		Assert.notNull(user, "User can't be null");
		Assert.notNull(user.getEmail(), "Email can't be null");
		Optional<User> userFromDB = userRepository.findByEmail(user.getEmail());
		if (userFromDB.isPresent() && !userFromDB.get().getId().equals(user.getId())) {
			throw new UserWithSameEmailExistException(user.getEmail());
		}
	}

	public User findByPseudo(String pseudo) throws UserWithSamePseudoExistException {
		Assert.notNull(pseudo, "Pseudo  can't be null");
		return userRepository.findByUserProfilePseudo(pseudo)
				.orElseThrow(() -> new UserWithSamePseudoExistException(pseudo));
	}

	public User update(User user) throws UserWithSamePseudoExistException, UserWithSameUsernameExistException,
			UserWithSameEmailExistException {

		checkIfUsernameAlreadyExists(user);
		checkIfEmailAlreadyExists(user);

		User existingUser = userRepository.findById(user.getId())
				.orElseThrow(() -> new EntityNotFoundException("User not found with id " + user.getId()));

		existingUser.setEmail(user.getEmail());
		existingUser.setEnabled(user.isEnabled());

		if (user.getUserProfile() != null) {
			updateProfile(user.getUserProfile());
		}

		return userRepository.save(existingUser);
	}

	public List<UserProfile> getUsersProfiles() {
		return userProfileRepository.findAll();
	}

	public void updatePassword(User authenticatedUser, PasswordChangeRequestDTO passwordChangeRequestDTO) {
		User user = userRepository.findByUsername(authenticatedUser.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		if (!passwordEncoder.matches(passwordChangeRequestDTO.getOldPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Ancien mot de passe incorrect");
		}

		user.setPassword(passwordEncoder.encode(passwordChangeRequestDTO.getNewPassword()));
		userRepository.save(user);

	}

	public void forgotPassword(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Can't find user with this email : " + email));

		String token = generatePasswordResetToken(email);

		mailService.sendPasswordResetEmail(user.getEmail(), token);
	}

	private String generatePasswordResetToken(String email) {
		return Jwts.builder().setSubject(email).setIssuedAt(new Date())
				.setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	public void resetPassword(String token, String newPassword) {
		Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

		String email = claims.getSubject();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec cet email : " + email));

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	private String generateVerificationEmailToken(String email) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + 24 * 60 * 60 * 1000);

		return Jwts.builder().setSubject(email).claim("type", "email_verification").setIssuedAt(now)
				.setExpiration(expiry).signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	public void verifyEmail(String token) {
		try {
			Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

			String email = claims.getSubject();
			String type = claims.get("type", String.class);

			if (!"email_verification".equals(type)) {
				throw new InvalidVerificationTokenException("Type de token invalide.");
			}

			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

			if (user.isEnabled()) {
				throw new EmailAlreadyVerifiedException("L'email est déjà vérifié.");
			}

			user.setEnabled(true);
			userRepository.save(user);

		} catch (ExpiredJwtException e) {
			throw new ExpiredVerificationTokenException("Le token a expiré.");
		} catch (JwtException e) {
			throw new InvalidVerificationTokenException("Token JWT invalide.");
		}
	}

}
