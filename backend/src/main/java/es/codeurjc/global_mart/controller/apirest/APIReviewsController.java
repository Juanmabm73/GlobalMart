package es.codeurjc.global_mart.controller.apirest;

import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.Reviewss.ReviewDTO;
import es.codeurjc.global_mart.dto.Reviewss.ReviewMapper;
import es.codeurjc.global_mart.service.ProductService;
import es.codeurjc.global_mart.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
public class APIReviewsController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    ReviewMapper reviewMapper;

    @Operation(summary = "Get all reviews of a product", description = "Retrieve all reviews for a specific product by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of reviews retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewsByProductId(@PathVariable Long id) {
        Optional<ProductDTO> product = productService.getProductById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(product.get().reviews());
    }

    @Operation(summary = "Get a specific review of a product", description = "Retrieve a specific review for a product by its ID and review ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or review not found")
    })
    @GetMapping("/{productId}/{reviewId}")
    public ResponseEntity<?> getReviewsOfAProductById(@PathVariable Long productId, @PathVariable Long reviewId) {
        Optional<ProductDTO> product = productService.getProductById(productId);
        if (product.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(product.get().reviews().stream()
                .filter(review -> review.getReviewId().equals(reviewId)).findFirst().orElse(null));
    }

    @Operation(summary = "Post a new review for a product", description = "Add a new review to a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Error adding review")
    })
    @PostMapping("/{id}")
    public ResponseEntity<?> postNewReview(
            @PathVariable Long id,
            @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(null);
        }

        Optional<ProductDTO> product = productService.getProductById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Object principal = authentication.getPrincipal();
        ReviewDTO review = null;
        String username = null;

        if (principal instanceof OAuth2User oAuth2User) {
            username = oAuth2User.getAttribute("name");
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            username = userDetails.getUsername();
        }

        if (username != null) {
            review = reviewService.addReview(reviewDTO, username);
        }

        if (review != null) {
            productService.addReviewToProduct(product.get(), review);
            System.out.println("Product: " + productService.getProductById(product.get().id()));
            return ResponseEntity.ok(review);
        } else {
            return ResponseEntity.status(500).body(null);
        }
    }
    /*
     * -------------------------------------FINISH IMPLEMENTATION
     * 
     * @DeleteMapping("/{productId}/{reviewId}")
     * public ResponseEntity<?> deleteReview(
     * 
     * @PathVariable Long productId,
     * 
     * @PathVariable Long reviewId,
     * Authentication authentication) {
     * 
     * if (authentication == null) {
     * return ResponseEntity.status(401).body("Unauthorized");
     * }
     * 
     * Optional<ProductDTO> product = productService.getProductById(productId);
     * if (product.isEmpty()) {
     * return ResponseEntity.status(404).body("Product not found");
     * }
     * 
     * ReviewDTO review = product.get().reviews().stream().filter(r ->
     * r.getReviewId().equals(reviewId)).findFirst().orElse(null);
     * if (review == null) {
     * return ResponseEntity.status(404).body("Review not found");
     * }
     * 
     * Object principal = authentication.getPrincipal();
     * String username = null;
     * 
     * if (principal instanceof OAuth2User oAuth2User) {
     * username = oAuth2User.getAttribute("name");
     * } else if (principal instanceof
     * org.springframework.security.core.userdetails.User userDetails) {
     * username = userDetails.getUsername();
     * }
     * 
     * if (username != null && review.username().equals(username)) {
     * productService.removeReviewFromProduct(product.get(), review);
     * productService.addProduct(product.get());
     * return ResponseEntity.ok("Review deleted successfully");
     * } else {
     * return ResponseEntity.status(403).body("Forbidden");
     * }
     * }
     * 
     */

}
