package es.codeurjc.global_mart.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.service.ProductService;
import es.codeurjc.global_mart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProductsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/products/allProducts")
    public String seeAllProds(Model model, HttpServletRequest request, Authentication authentication) {
        List<ProductDTO> products = productService.getAcceptedProducts(PageRequest.of(0, 5)).getContent();
        products = productService.addImageDataToProducts(products);
        model.addAttribute("allProds", products);
        model.addAttribute("tittle", false);
        model.addAttribute("hasNextProd", productService.getAcceptedProducts(PageRequest.of(1, 5)).hasContent());

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            model.addAttribute("allCompanyProds", Collections.emptyList());
        } else {
            Optional<UserDTO> user = userService.findByUsername(principal.getName());
            if (user.isPresent() && userService.isCompany(user.get())) {
                List<ProductDTO> companyProducts = productService
                        .getAcceptedCompanyProducts(user.get().username(), PageRequest.of(0, 5)).getContent();

                companyProducts = productService.addImageDataToProducts(companyProducts);

                model.addAttribute("isCompany", true);
                model.addAttribute("companyName", user.get().name());
                model.addAttribute("allCompanyProds", companyProducts);
            } else {
                model.addAttribute("allCompanyProds", Collections.emptyList());
            }
        }
        return "products";
    }

    @GetMapping("/moreProdsAll")
    public String loadMoreProducts(@RequestParam int page, Model model, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<ProductDTO> productsPage = productService.getAcceptedProducts(pageable);
        List<ProductDTO> products = productsPage.getContent();
        products = productService.addImageDataToProducts(products);

        model.addAttribute("hasMore", productsPage.hasNext());
        model.addAttribute("allProds", products);

        return "moreProducts";
    }

    @GetMapping("/moreProdsTypes")
    public String loadMoreProductsByType(@RequestParam int page, @RequestParam String type, Model model) {
        Pageable pageable = Pageable.ofSize(5).withPage(page);
        Page<ProductDTO> productsPage = productService.getAcceptedProductsByType(type, pageable);
        List<ProductDTO> products = productsPage.getContent();
        productService.addImageDataToProducts(products);
        model.addAttribute("hasMore", productsPage.getTotalPages() - 1 > page);
        model.addAttribute("allProds", products);
        model.addAttribute("type", type);
        return "moreProducts";
    }

    @GetMapping("/moreProdsCompany")
    public String loadMoreProductsByCompany(@RequestParam int page, @RequestParam String company, Model model) {
        Pageable pageable = Pageable.ofSize(5).withPage(page);
        Page<ProductDTO> productsPage = productService.getAcceptedCompanyProducts(company, pageable);
        List<ProductDTO> products = productsPage.getContent();
        productService.addImageDataToProducts(products);
        model.addAttribute("hasMore", productsPage.getTotalPages() - 1 > page);
        model.addAttribute("allProds", products);
        model.addAttribute("company", company);
        model.addAttribute("isCompany", true);
        return "moreProducts";
    }

    @GetMapping("/products/{type}")
    public String productsByType(@PathVariable String type, Model model) {

        List<ProductDTO> products = productService.getAcceptedProductsByType(type, PageRequest.of(0, 5)).getContent();
        products = productService.addImageDataToProducts(products);

        model.addAttribute("allProds", products);
        model.addAttribute("type", type);
        model.addAttribute("tittle", true);
        model.addAttribute("hasNextProd",
                productService.getAcceptedProductsByType(type, PageRequest.of(1, 5)).hasContent());
        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDescription(@PathVariable Long id, Model model, Authentication autentication)
            throws Exception {
        Optional<ProductDTO> product = productService.getProductById(id); // Extract the product by its id
        ProductDTO productWithImage = productService.addImageToASingleProduct(product.get());
        System.out.println("product image: " + productWithImage.imageBase64());
        product = Optional.of(productWithImage);

        if (product.isPresent()) {
            System.out.println("Hola");
            model.addAttribute("product", product.get()); // product dto contains all the product info review html
            // System.out.println("Product details" + product.get());

            productService.setViews_product_count(product.get());
            model.addAttribute("productImage", product.get().imageBase64());
            model.addAttribute("productId", product.get().id());
            model.addAttribute("productStock", product.get().stock());
            model.addAttribute("reviews", product.get().reviews());

            return "descriptionProduct";
        } else {
            System.out.println("Adios");
            return "redirect:/allProducts";
        }
    }

    @GetMapping("/new_product")
    public String new_product(Model model) {
        model.addAttribute("form_title", "New product");
        return "uploadProducts";
    }

    @PostMapping("/newproduct")
    public String newproduct(@RequestParam String product_name, @RequestParam MultipartFile product_image,
            @RequestParam String product_description, @RequestParam String product_type,
            @RequestParam Integer product_stock, @RequestParam Double product_price, Authentication autentication)
            throws Exception {
        Object principal = autentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            productService.createProduct(product_type, product_name, oAuth2User.getAttribute("name"),
                    product_price,
                    product_description,
                    BlobProxy.generateProxy(product_image.getInputStream(), product_image.getSize()),
                    product_stock, false, null);
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            productService.createProduct(product_type, product_name, userDetails.getUsername(),
                    product_price,
                    product_description,
                    BlobProxy.generateProxy(product_image.getInputStream(), product_image.getSize()),
                    product_stock, false, null);
        }

        return "redirect:/products/allProducts";
    }

    @GetMapping("/acceptProduct/{id}")
    public String acceptProduct(@PathVariable Long id) {
        Optional<ProductDTO> product = productService.getProductById(id);
        if (product.isPresent()) {
            productService.updateProduct(id, product.get().name(),
                    product.get().price());
        }
        return "redirect:/adminPage";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/adminPage";
    }

    @GetMapping("/edit_product/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("form_title", "Edit Product");

        Optional<ProductDTO> optionalProduct = productService.getProductById(id);
        if (optionalProduct.isPresent()) {
            // Product product = optionalProduct.get();
            userService.convertBlobToBase64(optionalProduct.get());

            // add the product to the model
            model.addAttribute("type_" + optionalProduct.get().type(), true);
            model.addAttribute("product", optionalProduct.get());
        } else {
            return "redirect:/products/allProducts";
        }

        return "uploadProducts";
    }

    @PostMapping("/update_product/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @RequestParam String product_name,
            @RequestParam(required = false) MultipartFile product_image,
            @RequestParam String product_description,
            @RequestParam String product_type,
            @RequestParam Integer product_stock,
            @RequestParam Double product_price,
            Authentication autentication)
            throws Exception {

        Optional<ProductDTO> optionalProduct = productService.getProductById(id);
        if (optionalProduct.isPresent()) {
            productService.updateProductDetails(optionalProduct.get(), product_name, product_description, product_type,
                    product_stock, product_price, product_image);

            Object principal = autentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
                Optional<UserDTO> user = userService.findByUsername(userDetails.getUsername());
                if (user.isPresent() && userService.isCompany(user.get())) {
                    return "redirect:/products/allProducts";
                } else {
                    return "redirect:/adminPage";
                }
            } else {
                return "redirect:/products/allProducts";
            }
        }
        return "redirect:/products/allProducts";
    }
}
