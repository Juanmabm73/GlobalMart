package es.codeurjc.global_mart.controller.apirest.auth;

import es.codeurjc.global_mart.security.jwt.AuthResponse.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.global_mart.security.jwt.AuthResponse;
import es.codeurjc.global_mart.security.jwt.LoginRequest;
import es.codeurjc.global_mart.security.jwt.LoginResponse;
import es.codeurjc.global_mart.security.jwt.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    @Autowired
    private UserLoginService userLoginService;

    @Operation(summary = "Login a user", description = "Authenticate a user and provide a JWT token along with user details upon successful login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful with user details and token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        // Check if user exists before attempting authentication
        if (userLoginService.getUserService().findByUsername(loginRequest.getUsername()).isEmpty()) {
            AuthResponse errorResponse = new AuthResponse(
                    AuthResponse.Status.FAILURE,
                    null,
                    "User not found",
                    null,
                    false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // If user exists, proceed with the normal login flow
        return userLoginService.login(response, loginRequest);
    }

    // Mantener los otros métodos igual
    @Operation(summary = "Refresh authentication token", description = "Refresh the user's authentication token using the refresh token stored in the cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Refresh successful, new token issued"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "RefreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            AuthResponse loginResponse = new AuthResponse(
                    AuthResponse.Status.FAILURE,
                    null,
                    "Refresh token is missing",
                    null,
                    false);
            return ResponseEntity.ok(loginResponse);
        }

        return userLoginService.refresh(response, refreshToken);
    }

    @Operation(summary = "Logout a user", description = "Logout the user by invalidating the session and refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful, no content returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, unable to logout")
    })
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
        return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response), null));
    }

    @Operation(summary = "Test login endpoint", description = "Check if the login endpoint is accessible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test endpoint accessible")
    })
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Login endpoint is accessible");
    }
}
