package es.codeurjc.global_mart.controller;

import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.model.User;
import es.codeurjc.global_mart.service.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // Asegurarse de importar Controller
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class LoginRegisterController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String registerUser(@RequestParam String name, @RequestParam String username, // receive the form data
            @RequestParam String mail,
            @RequestParam String password,
            @RequestParam MultipartFile image,
            @RequestParam String role) throws Exception {

        Optional<UserDTO> user = userService.findByUsername(username);
        if (user.isPresent()) {
            return "errors";
        } else {
            userService.createUser(image, name, username, mail, passwordEncoder.encode(password), List.of(role));
        }

        return "redirect:/"; // redirect to the profile page
    }

    @GetMapping("/loginComprobation")
    public String loginComprobation(OAuth2AuthenticationToken authentication) {
        OAuth2User oAuth2User = authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Optional<UserDTO> existingUser = userService.findByEmail(email);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(oAuth2User.getAttribute("name"));
            newUser.setEmail(email);
            // newUser.setImage(oAuth2User.getAttribute("picture"));
            newUser.setRole(List.of("USER")); // we set the role to USER

            userService.save(newUser);
        }

        return "redirect:/"; // redirect to the profile page
    }

}