package es.codeurjc.global_mart.controller;

import java.util.Optional;
import es.codeurjc.global_mart.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.Reviewss.ReviewDTO;
import es.codeurjc.global_mart.service.ProductService;

@Controller
public class ReviewsController {

    private final ReviewService reviewService;

    @Autowired
    private ProductService productService;

    ReviewsController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Function to add review to a product
    @PostMapping("/product/{id}/new_review")
    public String postMethodName(@RequestParam int calification, @RequestParam String comment,
            Authentication autentication, @PathVariable Long id) {

        Optional<ProductDTO> product = productService.getProductById(id);
        Object principal = autentication.getPrincipal();
        ReviewDTO review = null;
        String username = null;
        if (principal instanceof OAuth2User oAuth2User) {
            username = oAuth2User.getAttribute("name");
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            username = userDetails.getUsername();
        }

        review = reviewService.createReview(username, comment, calification);

        if (product.isPresent()) {
            productService.addReviewToProduct(product.get(), review);

            return "redirect:/product/" + id;
        } else {
            return "redirect:/error";
        }
    }
}
