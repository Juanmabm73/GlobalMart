package es.codeurjc.global_mart.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.web.csrf.CsrfToken;

import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.dto.User.UserMapper;
import es.codeurjc.global_mart.model.User;
import es.codeurjc.global_mart.security.RepositoryUserDetailsService;
import es.codeurjc.global_mart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RepositoryUserDetailsService userDetailsService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/edit_profile")
    public String showEditProfile(Model model, Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            return "redirect:/login";
        }

        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return "redirect:/";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("name", user.getName());
        model.addAttribute("userId", user.getId());

        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token != null) {
            model.addAttribute("token", token.getToken());
        }

        return "edit_profile";
    }

    @PostMapping("/edit_profile")
    public String updateProfile(
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            Authentication currentAuth) {

        Optional<UserDTO> optionalUser = userService.getUserById(userId);

        if (optionalUser.isPresent()) {

            UserDTO userDTO = optionalUser.get();
            User user = userMapper.toUser(userDTO);

            String oldUsername = user.getUsername();

            user.setUsername(username);
            user.setEmail(email);
            user.setName(name);

            if (password != null && !password.isEmpty() && password.equals(confirmPassword)) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userService.save(user);

            if (!oldUsername.equals(username) && currentAuth instanceof UsernamePasswordAuthenticationToken) {
                UserDetails newUserDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                        newUserDetails, null, newUserDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            return "redirect:/profile";
        } else {
            return "redirect:/";
        }
    }

    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            Optional<UserDTO> user = userService.findByEmail(email);
            return user.map(userMapper::toUser).orElse(null);
        } else if (principal instanceof UserDetails userDetails) {
            Optional<UserDTO> user = userService.findByUsername(userDetails.getUsername());
            return user.map(userMapper::toUser).orElse(null);
        }

        return null;
    }
}