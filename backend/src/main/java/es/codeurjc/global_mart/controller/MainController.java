package es.codeurjc.global_mart.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.service.ProductService;
import es.codeurjc.global_mart.service.UserService;

@Controller
public class MainController {

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private SearchController searchController;

	@GetMapping("/")
	public String greeting(Model model) {
		// get the most viewed and last products
		List<ProductDTO> mostViewedProducts = productService.getMostViewedProducts(4);
		List<ProductDTO> lastProducts = productService.getLastProducts();

		// Add image data to the products
		mostViewedProducts = productService.addImageDataToProducts(mostViewedProducts);
		lastProducts = productService.addImageDataToProducts(lastProducts);

		// Add the products to the model
		model.addAttribute("mostViewedProducts", mostViewedProducts);
		model.addAttribute("lastProducts", lastProducts);

		return "index";
	}

	@GetMapping("/redir")
	public String redir(Model model) {
		return "redirect:/";
	}

	// Redirection to the login page
	@GetMapping("/choose_login_option")
	public String choose_login(Model model) {
		return "choose_login_option";
	}

	@GetMapping("/login_error")
	public String login_error(Model model) {
		return "login_error";
	}

	// Redirection to the about us page
	@GetMapping("/about")
	public String about_us(Model model) {
		return "about";
	}

	@GetMapping("/adminPage")
	public String admin(Model model) {
		List<ProductDTO> products = productService.getNotAcceptedProducts();

		List<ProductDTO> productsWithImages = productService.addImageDataToProducts(products);

		model.addAttribute("productsNotAccepted", productsWithImages);

		return "administrator";
	}

	@GetMapping("/profile")
	public String profile(Model model, Authentication authentication) {
		if (authentication == null) {
			return "redirect:/login";
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof OAuth2User oAuth2User) {
			model.addAttribute("name", oAuth2User.getAttribute("name"));
			model.addAttribute("username", oAuth2User.getAttribute("name"));
			model.addAttribute("email", oAuth2User.getAttribute("email"));
			model.addAttribute("profile_image", oAuth2User.getAttribute("picture"));
			model.addAttribute("isGoogleUser", true); // add flag to indicate authentication with Google
		}
		// Regular user
		else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
			Optional<UserDTO> user = userService.findByUsername(userDetails.getUsername());

			if (user.isPresent()) {
				UserDTO userDTO = userService.addImageBase64ToUser(user.get());
				model.addAttribute("userDTO", userDTO);
				model.addAttribute("isGoogleUser", false); // add flag to indicate authentication with username and
															// password
			}
		}

		return "user";
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public SearchController getSearchController() {
		return searchController;
	}

	public void setSearchController(SearchController searchController) {
		this.searchController = searchController;
	}

}