package es.codeurjc.global_mart.controller.apirest;

import es.codeurjc.global_mart.dto.User.UserCreationDTO;
import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.dto.User.UserMapper;
import es.codeurjc.global_mart.model.User;
import es.codeurjc.global_mart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class APIUserController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Operation(summary = "Get all users", description = "Retrieve a list of all users in the system.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of users retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/")
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		List<UserDTO> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	@Operation(summary = "Get user by ID", description = "Retrieve a user by their unique ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
			@ApiResponse(responseCode = "404", description = "User not found")
	})
	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(@PathVariable Long id) {
		Optional<UserDTO> user = userService.getUserById(id);
		if (user.isEmpty()) {
			return ResponseEntity.status(404).body("User not found");
		}
		return ResponseEntity.ok(user.get());
	}

	@Operation(summary = "Create a new user", description = "Register a new user in the system.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
			@ApiResponse(responseCode = "400", description = "Username already exists"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PostMapping("/")
	public ResponseEntity<?> createUser(@RequestBody UserCreationDTO userDto) {
		Optional<UserDTO> user = userService.findByUsername(userDto.username());
		if (user.isPresent()) {
			return ResponseEntity.badRequest().body("Username already exists");
		} else {
			try {
				User createdUser = userService.createUser(null, userDto.name(), userDto.username(), userDto.email(),
						passwordEncoder.encode(userDto.password()), userDto.role());
				UserDTO createdUserDto = userMapper.toUserDTO(createdUser);

				// Crear un objeto que contenga el ID y el JSON completo del usuario
				Map<String, Object> response = new HashMap<>();
				response.put("id", createdUserDto.id());
				response.put("user", createdUserDto);

				return ResponseEntity.ok(response);
			} catch (IOException e) {
				return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
			}
		}
	}

	@Operation(summary = "Update user profile", description = "Update the details of an existing user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Profile updated successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "User not found")
	})
	@PutMapping("/")
	public ResponseEntity<?> updateProfile(
			@RequestBody UserCreationDTO userUpdateDTO,
			Authentication authentication,
			HttpServletResponse response) {

		System.out.println("llamada recibida");
		System.out.println("Datos usuario actualizado: " + userUpdateDTO);

		if (authentication == null) {
			return ResponseEntity.status(401).body("Unauthorized");
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
			System.out.println("Nombre anterior: " + userDetails.getUsername());

			// Obtener el usuario directamente del servicio en lugar de convertir desde DTO
			User user = userService.findUserByUsername(userDetails.getUsername());

			if (user != null) {
				boolean usernameChanged = false;

				// Actualizar campos solo si se proporcionan valores
				if (userUpdateDTO.username() != null && !userUpdateDTO.username().equals(user.getUsername())) {
					usernameChanged = true;
					user.setUsername(userUpdateDTO.username());
				}

				if (userUpdateDTO.name() != null) {
					user.setName(userUpdateDTO.name());
				}

				if (userUpdateDTO.email() != null) {
					user.setEmail(userUpdateDTO.email());
				}

				// Solo actualizar la contraseña si se proporciona una nueva
				if (userUpdateDTO.password() != null && !userUpdateDTO.password().isEmpty()) {
					user.setEncodedPassword(passwordEncoder.encode(userUpdateDTO.password()));
				}
				// La contraseña existente se mantiene porque estamos usando el usuario original
				// de la base de datos

				System.out.println("Usuario actualizado: " + user);
				userService.save(user);

				// Si el nombre de usuario cambió, indicarlo en la respuesta
				if (usernameChanged) {
					return ResponseEntity.status(205).body(userMapper.toUserDTO(user));
				}

				return ResponseEntity.ok(userMapper.toUserDTO(user));
			} else {
				return ResponseEntity.status(404).body("User not found");
			}
		}

		return ResponseEntity.status(404).body("User not found");
	}

	@Operation(summary = "Delete user", description = "Delete a user by their unique ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User deleted successfully"),
			@ApiResponse(responseCode = "404", description = "User not found")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		Optional<UserDTO> user = userService.getUserById(id);
		if (user.isEmpty()) {
			return ResponseEntity.status(404).body("User not found");
		}
		userService.deleteUser(id);
		return ResponseEntity.ok("User deleted successfully");
	}

	@Operation(summary = "Get user ID by username", description = "Retrieve a user's ID by their username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User ID retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
			@ApiResponse(responseCode = "404", description = "User not found")
	})
	@GetMapping("/by-username/{username}")
	public ResponseEntity<Long> getUserIdByUsername(@PathVariable String username) {
		Optional<UserDTO> user = userService.findByUsername(username);
		if (user.isEmpty()) {
			return ResponseEntity.status(404).build();
		}
		// Retornamos SOLO el ID como un número, no como parte de un objeto
		return ResponseEntity.ok(user.get().id());
	}

	// ------------------------------------------Images------------------------------------------

	@Operation(summary = "Get user image", description = "Retrieve the image of a user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User image retrieved successfully", content = @Content(mediaType = "image/jpeg")),
			@ApiResponse(responseCode = "404", description = "User image not found")
	})
	@GetMapping("/{id}/image")
	public ResponseEntity<Object> getUserImage(@PathVariable long id) throws SQLException, IOException {

		Resource postImage = userService.getUserImage(id);

		if (postImage == null) {
			return ResponseEntity.status(404).body("User image not found");
		}

		return ResponseEntity
				.ok()
				.header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
				.body(postImage);

	}

	@Operation(summary = "Upload user image", description = "Upload a new image for a user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
			@ApiResponse(responseCode = "400", description = "Image already exists")
	})
	@PostMapping("/{id}/image")
	public ResponseEntity<?> createUserImage(@PathVariable long id, @RequestParam MultipartFile imageFile)
			throws SQLException, IOException {

		userService.createUserImage(id, imageFile.getInputStream(), imageFile.getSize());
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Replace user image", description = "Replace the existing image for a user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Image replaced successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "400", description = "Image doesn't exist")
	})
	@PutMapping("/{id}/image")
	public ResponseEntity<?> replaceProductImage(@PathVariable long id, @RequestParam MultipartFile imageFile,
			Authentication authentication)
			throws SQLException, IOException {

		if (authentication == null)
			return ResponseEntity.status(401).body("Unauthorized");

		if (userService.getUserImage(id) == null) {
			return ResponseEntity.badRequest().body("Image doesn't exists");
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			if (oAuth2User.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| userService.getUserById(id).get().username().equals(oAuth2User.getAttribute("name"))) {
				userService.createUserImage(id, imageFile.getInputStream(), imageFile.getSize());
				return ResponseEntity.ok().build();
			}

		} else if (principal instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
			if (userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| userService.getUserById(id).get().username().equals(userDetails.getUsername())) {
				userService.createUserImage(id, imageFile.getInputStream(), imageFile.getSize());
				return ResponseEntity.ok().build();
			}
		}

		return ResponseEntity.status(403).body("Forbidden");

	}

	@Operation(summary = "Delete user image", description = "Delete a user's image.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User image deleted successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "Image doesn't exist"),
			@ApiResponse(responseCode = "403", description = "Forbidden")
	})
	@DeleteMapping("/{id}/image")
	public ResponseEntity<?> deleteProductImage(@PathVariable long id, Authentication authentication)
			throws SQLException, IOException {

		if (authentication == null)
			return ResponseEntity.status(401).body("Unauthorized");

		if (userService.getUserImage(id) == null) {
			return ResponseEntity.badRequest().body("Image doesn't exists");
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			if (oAuth2User.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| userService.getUserById(id).get().username().equals(oAuth2User.getAttribute("name"))) {
				userService.deleteUserImage(id);
				return ResponseEntity.ok().build();
			}

		} else if (principal instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
			if (userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| userService.getUserById(id).get().username().equals(userDetails.getUsername())) {
				userService.deleteUserImage(id);
				return ResponseEntity.ok().build();
			}
		}

		return ResponseEntity.status(403).body("Forbidden");
	}

	@Operation(summary = "Get user profile", description = "Retrieve the profile details of the authenticated user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "User not found")
	})
	@GetMapping("/profile")
	public ResponseEntity<?> getProfile(Authentication authentication) {
		if (authentication == null) {
			return ResponseEntity.status(401).body(null);
		}
		Object principal = authentication.getPrincipal();

		if (principal instanceof OAuth2User oAuth2User) {
			// Para usuarios OAuth2, necesitamos buscar o crear el usuario en nuestra base
			// de datos
			Optional<UserDTO> existingUser = userService.findByUsername(oAuth2User.getAttribute("name"));

			if (existingUser.isPresent()) {
				return ResponseEntity.ok(existingUser.get());
			} else {
				return ResponseEntity.status(404).body(null);
			}
		} else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
			Optional<UserDTO> user = userService.findByUsername(userDetails.getUsername());
			if (user.isPresent()) {
				return ResponseEntity.ok(user.get());
			}
		}

		return ResponseEntity.status(404).body(null);
	}

}
