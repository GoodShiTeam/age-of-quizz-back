package goodshi.ageofquizz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import goodshi.ageofquizz.entity.User;

@Service
public class AuthenticationFacade {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Return authenticated User or null if not authenticated.
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return customUserDetailsService.findByUsername(userDetails.getUsername());
        }
        return null;
    }
}
