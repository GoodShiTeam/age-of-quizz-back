package goodshi.ageofquizz.websocket;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import goodshi.ageofquizz.authentication.JwtUtil;
import goodshi.ageofquizz.service.CustomUserDetailsService;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		try {
			String query = request.getURI().getQuery();
			if (query != null && query.startsWith("token=")) {
				String token = query.substring("token=".length());
				String username = jwtUtil.extractUsername(token);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtUtil.validateToken(token, userDetails)) {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());

					attributes.put("user", auth);
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String guestId = UUID.randomUUID().toString();

		UsernamePasswordAuthenticationToken guestAuth = new UsernamePasswordAuthenticationToken("guest_" + guestId,
				null, null);

		attributes.put("user", guestAuth);

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
	}
}
