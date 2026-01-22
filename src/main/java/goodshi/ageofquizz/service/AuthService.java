package goodshi.ageofquizz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.authentication.AuthCredentialsRequest;

@Service
public class AuthService {
	@Autowired
	private AuthenticationManager authenticationManager;

	public UserDetails getUserFromRequest(AuthCredentialsRequest request) {
		return (UserDetails) authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
				.getPrincipal();
	}

}
