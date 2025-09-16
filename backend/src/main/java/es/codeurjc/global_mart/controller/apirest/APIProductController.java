package es.codeurjc.global_mart.controller.apirest;

import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.model.Product;
import es.codeurjc.global_mart.repository.ProductRepository;
import es.codeurjc.global_mart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.global_mart.dto.Product.ProductMapper;
import es.codeurjc.global_mart.dto.Product.SearchProductDTO;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/products")
public class APIProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private ProductRepository productRepository;
	// ------------------------------------------BASIC
	// CRUD------------------------------------------

	@Operation(summary = "Get products", description = "Retrieve products based on filters.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
			@ApiResponse(responseCode = "404", description = "No products found")
	})
	@GetMapping("/")
	public ResponseEntity<Page<ProductDTO>> getProducts(
			@RequestParam(name = "accepted", required = false) Boolean accepted,
			@RequestParam(name = "company", required = false) String company,
			@PageableDefault(size = 5) Pageable pageable,
			Authentication authentication) {

		Page<ProductDTO> products;

		if (accepted && company != null) {
			if (authentication == null || authentication.getAuthorities().stream()
					.noneMatch(a -> a.getAuthority().equals("ROLE_COMPANY"))) {
				return ResponseEntity.status(403).build();
			}
			products = productService.getAcceptedCompanyProducts(company, pageable);
		} else if (company == null && !accepted) {
			if (authentication == null || authentication.getAuthorities().stream()
					.noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
				return ResponseEntity.status(403).build();
			}
			products = productRepository.findByIsAcceptedFalse(pageable)
					.map(productMapper::toProductDTO);
		} else {
			products = productRepository.findByIsAcceptedTrue(pageable)
					.map(productMapper::toProductDTO);
		}

		return ResponseEntity.ok(products);
	}

	@Operation(summary = "Get all accepted products", description = "Retrieve all accepted products without pagination.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of all accepted products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
	})
	@GetMapping("/all")
	public ResponseEntity<List<ProductDTO>> getAllProducts(
			@RequestParam(name = "accepted", required = false) Boolean accepted) {

		List<ProductDTO> products;
		if (accepted == false) {
			// Convertir List<Product> a List<ProductDTO>
			List<Product> notAcceptedProducts = productRepository.findByIsAcceptedFalse();
			products = notAcceptedProducts.stream()
					.map(productMapper::toProductDTO)
					.collect(java.util.stream.Collectors.toList());
		} else {
			// Convertir List<Product> a List<ProductDTO>
			List<Product> acceptedProducts = productRepository.findByIsAcceptedTrue();
			products = acceptedProducts.stream()
					.map(productMapper::toProductDTO)
					.collect(java.util.stream.Collectors.toList());
		}

		return ResponseEntity.ok(products);
	}

	@Operation(summary = "Get products by type", description = "Retrieve products filtered by type.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
	})
	@GetMapping("/type")
	public ResponseEntity<Page<ProductDTO>> getProductsByType(
			@RequestParam(name = "type", required = true) String type,
			@PageableDefault(size = 5) Pageable pageable) {
		Page<ProductDTO> products = productRepository.findByIsAcceptedTrueAndType(type, pageable)
				.map(productMapper::toProductDTO);
		return ResponseEntity.ok(products);
	}

	@Operation(summary = "Get products by type", description = "Retrieve products filtered by type.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
	})
	@GetMapping("/type/all")
	public ResponseEntity<List<ProductDTO>> getAllProductsByType(
			@RequestParam(name = "type", required = true) String type) {
		// Utilizar repository.findByIsAcceptedTrueAndType para obtener List<Product>
		List<Product> productList = productRepository.findByIsAcceptedTrueAndType(type);

		// Convertir List<Product> a List<ProductDTO>
		List<ProductDTO> products = productList.stream()
				.map(productMapper::toProductDTO)
				.collect(java.util.stream.Collectors.toList());

		return ResponseEntity.ok(products);
	}

	@Operation(summary = "Get product by ID", description = "Retrieve a product by its unique ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
			@ApiResponse(responseCode = "404", description = "Product not found")
	})
	@GetMapping("/{id}")
	public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
		Optional<ProductDTO> product = productService.getProductById(id);
		if (product.isPresent()) {
			return ResponseEntity.ok(product.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Create a new product", description = "Create a new product with a given set of properties.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden")
	})
	@PostMapping("/")
	public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO, Authentication authentication)
			throws IOException {

		if (authentication == null) {
			return ResponseEntity.status(401).body(null);
		} else if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_COMPANY"))) {
			return ResponseEntity.status(403).body(null);
		}

		Object principal = authentication.getPrincipal();
		ProductDTO productDTOfinal = null;
		String username = null;

		if (principal instanceof OAuth2User oAuth2User) {
			username = oAuth2User.getAttribute("name");
		} else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
			username = userDetails.getUsername();
		}

		if (username != null) {
			productDTOfinal = productService.addProduct(productDTO, username);
		}

		return ResponseEntity.ok(productDTOfinal);
	}

	@Operation(summary = "Update a product", description = "Update the details of an existing product.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden")
	})
	@PutMapping("/{id}")
	public ResponseEntity<ProductDTO> updateProduct(@PathVariable long id, @RequestBody ProductDTO productDTO,
			Authentication authentication) throws SQLException {

		if (authentication == null)
			return ResponseEntity.status(401).body(null);

		Object principal = authentication.getPrincipal();

		if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			if (oAuth2User.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(oAuth2User.getAttribute("name"))) {
				return ResponseEntity.ok(productService.updateProduct(id, productDTO));
			}

		} else if (principal instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
			if (userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(userDetails.getUsername())) {
				return ResponseEntity.ok(productService.updateProduct(id, productDTO));
			}
		}

		return ResponseEntity.status(403).body(null);
	}

	@Operation(summary = "Delete a product", description = "Delete a product by its unique ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product deleted successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Product not found")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<ProductDTO> deleteProduct(@PathVariable long id, Authentication authentication) {

		if (authentication == null)
			return ResponseEntity.status(401).body(null);

		Object principal = authentication.getPrincipal();

		// Check if the principal is an OAuth2User (authenticated via OAuth2)
		if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			// If the user is not the owner of the product, deny the operation
			if (oAuth2User.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(oAuth2User.getAttribute("name"))) {
				return ResponseEntity.ok(productService.deleteProduct(id));
			}

		} else if (principal instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
			if (userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(userDetails.getUsername())) {
				return ResponseEntity.ok(productService.deleteProduct(id));
			}
		}

		return ResponseEntity.status(403).body(null);
	}

	// ------------------------------------------Images------------------------------------------

	@Operation(summary = "Get product image", description = "Retrieve the image of a specific product by its ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product image retrieved successfully", content = @Content(mediaType = "image/jpeg")),
			@ApiResponse(responseCode = "404", description = "Product image not found")
	})
	@GetMapping("/{id}/image")
	public ResponseEntity<Object> getProductImage(@PathVariable long id) throws SQLException, IOException {

		Resource postImage = productService.getProductImage(id);

		System.out.println("Image: " + postImage);

		if (postImage == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity
				.ok()
				.header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
				.body(postImage);

	}

	@Operation(summary = "Upload product image", description = "Upload a new image for a specific product.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product image uploaded successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "400", description = "Bad request")
	})
	@PostMapping("/{id}/image")
	public ResponseEntity<ProductDTO> createProductImage(@PathVariable long id, @RequestParam("file") MultipartFile imageFile,
			Authentication authentication)
			throws IOException {

		if (authentication == null)
			return ResponseEntity.status(401).body(null);

		if (productService.getImageById(id) != null) {
			return ResponseEntity.badRequest().body(null);
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			if (oAuth2User.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(oAuth2User.getAttribute("name"))) {
				productService.createProductImage(id, imageFile.getInputStream(), imageFile.getSize());
				return ResponseEntity.ok().build();
			}

		} else if (principal instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
			if (userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(userDetails.getUsername())) {
				productService.createProductImage(id, imageFile.getInputStream(), imageFile.getSize());
				return ResponseEntity.ok().build();
			}
		}

		return ResponseEntity.status(403).body(null);

	}

	@Operation(summary = "Replace product image", description = "Replace the existing image of a product with a new one.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product image replaced successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "400", description = "Bad request")
	})
	@PutMapping("/{id}/image")
	public ResponseEntity<ProductDTO> replaceProductImage(@PathVariable long id,
			@RequestParam("file") MultipartFile imageFile,
			Authentication authentication)
			throws IOException {

		if (authentication == null)
			return ResponseEntity.status(401).body(null);

		if (productService.getImageById(id) == null) {
			return ResponseEntity.badRequest().body(null);
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			if (oAuth2User.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(oAuth2User.getAttribute("name"))) {
				productService.replaceProductImage(id, imageFile.getInputStream(), imageFile.getSize());
				return ResponseEntity.ok().build();
			}

		} else if (principal instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
			if (userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(userDetails.getUsername())) {
				productService.replaceProductImage(id, imageFile.getInputStream(), imageFile.getSize());
				return ResponseEntity.ok().build();
			}
		}

		return ResponseEntity.status(403).body(null);

	}

	@Operation(summary = "Delete product image", description = "Delete the image of a specific product.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Product image deleted successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "400", description = "Bad request")
	})
	@DeleteMapping("/{id}/image")
	public ResponseEntity<ProductDTO> deleteProductImage(@PathVariable long id, Authentication authentication)
			throws IOException {

		if (authentication == null)
			return ResponseEntity.status(401).body(null);

		if (productService.getImageById(id) == null) {
			return ResponseEntity.badRequest().body(null);
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			if (oAuth2User.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(oAuth2User.getAttribute("name"))) {
				productService.deleteProductImage(id);
				return ResponseEntity.ok().build();
			}

		} else if (principal instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
			if (userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
					|| productService.getProductById(id).get().company().equals(userDetails.getUsername())) {
				productService.deleteProductImage(id);
				return ResponseEntity.ok().build();
			}
		}

		return ResponseEntity.status(403).body(null);

	}

	// ------------------------------------------ALGORITHM------------------------------------------

	@Operation(summary = "Get most viewed products", description = "Retrieve a list of the most viewed products.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of most viewed products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
	})
	@GetMapping("/mostViewedProducts")
	public ResponseEntity<List<ProductDTO>> getMostViewedProducts() {
		List<ProductDTO> mostViewedProducts = productService.getMostViewedProducts(4);
		return ResponseEntity.ok(mostViewedProducts);
	}

	@Operation(summary = "Get last products", description = "Retrieve a list of the last added products.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of last products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
	})
	@GetMapping("/lastProducts")
	public ResponseEntity<List<Product>> getLastProducts() {
		List<ProductDTO> lastProducts = productService.getLastProducts();
		addImageDataToProducts(lastProducts);
		List<Product> products = productMapper.toProducts(lastProducts);
		return ResponseEntity.ok(products);
	}

	@PutMapping("/accept")
	public ResponseEntity<ProductDTO> acceptProduct(@RequestParam Long id) {
		Optional<Product> productOptional = productRepository.findById(id);

		if (productOptional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Product product = productOptional.get();
		product.setIsAccepted(true);
		productRepository.save(product);

		ProductDTO productDTO = productMapper.toProductDTO(product);
		return ResponseEntity.ok(productDTO);
	}

	@PutMapping("/addViewsCount")
	public ResponseEntity<ProductDTO> addViewsCount(@RequestParam Long id) {
		System.out.println("ID recibido: " + id); // Log para depuración

		Optional<Product> productOptional = productRepository.findById(id);

		if (productOptional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Product product = productOptional.get();
		product.setViewsCount(product.getViewsCount() + 1);
		productRepository.save(product);

		ProductDTO productDTO = productMapper.toProductDTO(product);
		return ResponseEntity.ok(productDTO);
	}

	@DeleteMapping("/delete")
	public ResponseEntity<Void> deleteProduct(@RequestParam Long id) {
		Optional<Product> productOptional = productRepository.findById(id);

		if (productOptional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		productRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	private void addImageDataToProducts(List<ProductDTO> products) {
		for (ProductDTO product : products) {
			addImageDataToProduct(product);
		}
	}

	private void addImageDataToProduct(ProductDTO productDTO) {
		try {
			Product product = productMapper.toProduct(productDTO);
			Blob imageBlob = product.getImage();
			if (imageBlob != null) {
				byte[] bytes = imageBlob.getBytes(1, (int) imageBlob.length());
				String imageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
				product.setImageBase64(imageBase64);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/search")
	public ResponseEntity<List<SearchProductDTO>> searchProducts(
			@RequestParam(required = false) String search_text,
			@RequestParam(required = false, defaultValue = "all") String type) {

		List<SearchProductDTO> searchResults;

		if (search_text != null && !search_text.isEmpty()) {
			// Buscar por texto
			if ("all".equalsIgnoreCase(type)) {
				searchResults = productService.searchProductsByName(search_text);
			} else {
				// Buscar por texto y tipo
				searchResults = productService.searchProductsByNameAndType(search_text, type);
			}
		} else if (!"all".equalsIgnoreCase(type)) {
			// Buscar por tipo
			searchResults = productService.getProductsByTypeToSearch(type);
		} else {
			// Todos los productos
			searchResults = productService.getAllProductsToSearch();
		}

		return ResponseEntity.ok(searchResults);
	}

}
