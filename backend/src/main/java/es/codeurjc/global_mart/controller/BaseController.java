package es.codeurjc.global_mart.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.security.CSRFHandlerConfiguration;
import es.codeurjc.global_mart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class BaseController {

    private final CSRFHandlerConfiguration CSRFHandlerConfiguration;

    @Autowired
    private UserService userService;

    BaseController(CSRFHandlerConfiguration CSRFHandlerConfiguration) {
        this.CSRFHandlerConfiguration = CSRFHandlerConfiguration;
    }

    @ModelAttribute
    public void addUserAttributes(Authentication authentication, org.springframework.ui.Model model,
            HttpServletRequest request) {

        model.addAttribute("logged", false);
        model.addAttribute("isAdmin", false);
        model.addAttribute("isCompany", false);
        model.addAttribute("isUser", false);
        model.addAttribute("isGoogleUser", false);

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            model.addAttribute("logged", true);
            Object principal = authentication.getPrincipal();

            // OAuth2 user (Google, GitHub, etc.)
            if (principal instanceof OAuth2User oAuth2User) {
                model.addAttribute("username", oAuth2User.getAttribute("name"));
                model.addAttribute("email", oAuth2User.getAttribute("email"));
                model.addAttribute("profile_image", oAuth2User.getAttribute("picture"));
                model.addAttribute("isUser", true);
                model.addAttribute("isGoogleUser", true);
            }
            // Regular user
            else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
                Optional<UserDTO> user = userService.findByUsername(userDetails.getUsername());
                if (user.isPresent()) {
                    model.addAttribute("username", user.get().username());
                    model.addAttribute("email", user.get().email());
                    // model.addAttribute("profile_image", user.get().image()); // TO
                    // FIX--------------------------------
                    model.addAttribute("isGoogleUser", false);

                    if (userService.isAdmin(user.get())) {
                        model.addAttribute("isAdmin", true);
                        model.addAttribute("isCompany", false);
                        model.addAttribute("isUser", false);
                    } else if (userService.isCompany(user.get())) {
                        model.addAttribute("isAdmin", false);
                        model.addAttribute("isCompany", true);
                        model.addAttribute("isUser", false);
                    } else {
                        model.addAttribute("isAdmin", false);
                        model.addAttribute("isCompany", false);
                        model.addAttribute("isUser", true);
                    }
                }
            }
        }
    }

    public CSRFHandlerConfiguration getCSRFHandlerConfiguration() {
        return CSRFHandlerConfiguration;
    }

}