package es.codeurjc.global_mart.controller.apirest;

import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.User.ShoppingCartDTO;
import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.service.OrderService;
import es.codeurjc.global_mart.service.ProductService;
import es.codeurjc.global_mart.service.UserService;
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

@RestController
@RequestMapping("/api/v1/users")
public class APIShoppingCartController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Get shopping cart of a user", description = "Retrieve the shopping cart details of a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shopping cart retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShoppingCartDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/shoppingcarts")
    public ResponseEntity<?> getShoppingCart(@RequestParam Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(null);
        }

        UserDTO user = getUserFromAuthentication(authentication);
        System.out.println("User Id: " + userService.getUserId(user));

        if (user != null && userService.getUserId(user) == id) {
            ShoppingCartDTO shoppingCart = userService.getShoppingCartData(user);
            productService.addImageDataToProducts(shoppingCart.cartProducts());
            return ResponseEntity.ok(shoppingCart);
        }

        return ResponseEntity.status(404).body(null);

    }

    @Operation(summary = "Add product to shopping cart", description = "Add a product to the shopping cart of a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to cart successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User or product not found")
    })
    @PostMapping("/shoppingcarts/{productId}")
    public ResponseEntity<ProductDTO> addProductToCart(@RequestParam Long id, @PathVariable Long productId,
            Authentication authentication) {
        System.out.println("Hola");
        if (authentication == null) {
            return ResponseEntity.status(401).body(null);
        }

        UserDTO user = getUserFromAuthentication(authentication);

        System.out.println("User Id: " + userService.getUserId(user));
        System.out.println("User Id by path: " + id);

        if (user != null && userService.getUserId(user) == id) {
            ProductDTO product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            userService.addProductToCart(user, product);
            return ResponseEntity.ok(product);
        }

        return ResponseEntity.status(404).body(null);
    }

    @Operation(summary = "Remove product from shopping cart", description = "Remove a product from the shopping cart of a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product removed from cart successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product or user not found")
    })
    @DeleteMapping("/shoppingcarts/{productId}")
    public ResponseEntity<ProductDTO> removeProductFromCart(@RequestParam Long id, @PathVariable Long productId,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(null);
        }

        UserDTO user = getUserFromAuthentication(authentication);

        // Check if the authenticated user matches the requested user ID
        if (user == null || userService.getUserId(user) != id) {
            return ResponseEntity.status(403).body(null); // Forbidden if user doesn't match
        }

        try {
            ProductDTO product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

            if (userService.productInCart(user, product)) {
                userService.removeProductFromCart(user, product);
                return ResponseEntity.ok(product);
            } else {
                return ResponseEntity.status(404).body(null); // Not found if product isn't in cart
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Internal error for other exceptions
        }
    }

    @Operation(summary = "Process payment for shopping cart", description = "Process payment for the items in the shopping cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/shoppingcarts/payment")
    public ResponseEntity<?> payment(Authentication authentication) {

        UserDTO user = getUserFromAuthentication(authentication);

        orderService.createOrder(user);

        return ResponseEntity.ok(null);
    }

    private UserDTO getUserFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            return userService.findByUsername(oAuth2User.getAttribute("name"))
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        return null;
    }
}
