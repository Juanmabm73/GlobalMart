package es.codeurjc.global_mart.security.jwt;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.model.User;
import es.codeurjc.global_mart.service.UserService;
import es.codeurjc.global_mart.dto.User.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserLoginService {

	private static final Logger log = LoggerFactory.getLogger(UserLoginService.class);

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserMapper userMapper;
	private final UserService userService;

	public UserLoginService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			JwtTokenProvider jwtTokenProvider, UserService userService, UserMapper userMapper) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userService = userService;
		this.userMapper = userMapper;
	}

	public UserService getUserService() {
		return userService;
	}

	public ResponseEntity<AuthResponse> login(HttpServletResponse response, LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String username = loginRequest.getUsername();
		UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

		Optional<UserDTO> userDTOOptional = userService.findByUsername(username);
		if (userDTOOptional.isEmpty()) {
			throw new RuntimeException("User not found: " + username);
		}
		UserDTO userDTO = userDTOOptional.get();
		User userEntity = userMapper.toUser(userDTO);

		HttpHeaders responseHeaders = new HttpHeaders();
		var newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
		var newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

		response.addCookie(buildTokenCookie(TokenType.ACCESS, newAccessToken));
		response.addCookie(buildTokenCookie(TokenType.REFRESH, newRefreshToken));

		// Asegúrate que el constructor de AuthResponse sea compatible con estos
		// parámetros
		AuthResponse loginResponse = new AuthResponse(
				AuthResponse.Status.SUCCESS,
				"Auth successful. Tokens are created in cookie.",
				null,
				userDTO, // Ahora es un UserDTO
				true);

		return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
	}

	public ResponseEntity<AuthResponse> refresh(HttpServletResponse response, String refreshToken) {
		try {
			var claims = jwtTokenProvider.validateToken(refreshToken);
			UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());
			Optional<UserDTO> userDTOOptional = userService.findByUsername(claims.getSubject());
			if (userDTOOptional.isEmpty()) {
				throw new RuntimeException("User not found: " + claims.getSubject());
			}
			User userEntity = userMapper.toUser(userDTOOptional.get());

			var newAccessToken = jwtTokenProvider.generateAccessToken(user);
			response.addCookie(buildTokenCookie(TokenType.ACCESS, newAccessToken));

			AuthResponse loginResponse = new AuthResponse(
					AuthResponse.Status.SUCCESS,
					"Auth successful. Tokens are created in cookie.",
					null,
					userMapper.toUserDTO(userEntity),
					true);

			return ResponseEntity.ok().body(loginResponse);

		} catch (Exception e) {
			log.error("Error while processing refresh token", e);
			AuthResponse loginResponse = new AuthResponse(
					AuthResponse.Status.FAILURE,
					null,
					"Failure while processing refresh token",
					null,
					false);

			return ResponseEntity.ok().body(loginResponse);
		}
	}

	public String logout(HttpServletResponse response) {
		SecurityContextHolder.clearContext();
		response.addCookie(removeTokenCookie(TokenType.ACCESS));
		response.addCookie(removeTokenCookie(TokenType.REFRESH));

		return "logout successfully";
	}

	private Cookie buildTokenCookie(TokenType type, String token) {
		Cookie cookie = new Cookie(type.cookieName, token);
		cookie.setMaxAge((int) type.duration.getSeconds());
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "None"); // Corregido de setAtrribute a setAttribute
		return cookie;
	}

	private Cookie removeTokenCookie(TokenType type) {
		Cookie cookie = new Cookie(type.cookieName, "");
		cookie.setMaxAge(0);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setSecure(true);
		return cookie; // Faltaba el return
	}
}