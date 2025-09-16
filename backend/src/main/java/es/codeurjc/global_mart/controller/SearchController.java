package es.codeurjc.global_mart.controller;

import es.codeurjc.global_mart.dto.Product.SearchProductDTO;
import es.codeurjc.global_mart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private ProductService productService;

    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(required = false) String search_text,
            @RequestParam(required = false, defaultValue = "all") String type,
            Model model) {

        List<SearchProductDTO> searchResults;
        System.out.println("Query: " + search_text);
        System.out.println("Type: " + type);

        if (search_text != null && !search_text.isEmpty()) {
            // by text
            if ("all".equals(type)) {
                searchResults = productService.searchProductsByName(search_text);
            } else {
                // by text and type
                searchResults = productService.searchProductsByNameAndType(search_text, type);
            }
        } else if (!"all".equals(type)) {
            // by type
            searchResults = productService.getProductsByTypeToSearch(type);
        } else {
            // all products
            searchResults = productService.getAllProductsToSearch();
        }

        // Convert the image to base64
        searchResults = productService.addImageDataToSearchProducts(searchResults);

        model.addAttribute("products", searchResults);
        model.addAttribute("searchQuery", search_text);
        model.addAttribute("category", type);

        return "search";
    }
}