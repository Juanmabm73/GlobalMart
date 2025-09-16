package es.codeurjc.global_mart.controller.apirest;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.global_mart.dto.Product.CompanyStadsDTO;
import es.codeurjc.global_mart.dto.User.HistoricalOrdersDTO;
import es.codeurjc.global_mart.service.ProductService;
import es.codeurjc.global_mart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/graphs")
public class APIGraphsController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Operation(summary = "Get company statistics graph", description = "Retrieve the company statistics data for graph display.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Company data not found")
    })
    @GetMapping("/companyGraph")
    public ResponseEntity<List<CompanyStadsDTO>> displayGraph(Authentication authentication) {
        String username = getUsername(authentication);
        if (username == null) {
            return ResponseEntity.status(401).body(null);
        }

        try {
            List<CompanyStadsDTO> dataList = productService.getCompanyStadistics(username);
            return ResponseEntity.ok(dataList);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @Operation(summary = "Get user historical orders graph", description = "Retrieve the user's historical orders data for graph display.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User historical orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/userGraph")
    public ResponseEntity<List<Double>> displayUserGraph(Authentication authentication) {
        String username = getUsername(authentication);
        if (username == null) {
            return ResponseEntity.status(401).body(null);
        }

        try {
            HistoricalOrdersDTO orderPrices = userService.getUserStads(username);
            return ResponseEntity.ok(orderPrices.historicalOrdersPrices());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    private String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            return oAuth2User.getAttribute("name");
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userDetails.getUsername();
        }

        return null;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.status(404).body(null);
    }
}
