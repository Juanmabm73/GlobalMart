package es.codeurjc.global_mart.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Date;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.global_mart.dto.Product.CompanyStadsDTO;
import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.Product.ProductMapper;
import es.codeurjc.global_mart.dto.Product.SearchProductDTO;
import es.codeurjc.global_mart.dto.Reviewss.ReviewDTO;
import es.codeurjc.global_mart.dto.Reviewss.ReviewMapper;
import es.codeurjc.global_mart.model.Product;
import es.codeurjc.global_mart.model.Review;
import es.codeurjc.global_mart.repository.ProductRepository;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    ReviewMapper reviewMapper;

    public ProductDTO createProduct(String type, String name, String business, Double price, String description,
            Blob image, Integer stock, Boolean isAccepted, List<Review> reviews) throws IOException {
        Product product = new Product(type, name, business, price, description, image, stock, isAccepted);
        product.setReviews(reviews);

        productRepository.save(product);
        return productMapper.toProductDTO(product);
    }

    // Add to your addProduct method
    public ProductDTO addProduct(ProductDTO productDTO, String username) {
        // Convert DTO to entity
        Product product = productMapper.toProduct(productDTO);

        // Set defaults for new products
        product.setDate(new java.sql.Timestamp(new Date().getTime())); // Set creation date to current time
        product.setViews_count(0); // Initialize views count to 0
        product.setCompany(username); // Set company name from username
        product.setIsAccepted(false); // Products start as not accepted

        // Save the product
        Product savedProduct = productRepository.save(product);

        // Convert back to DTO and return
        return productMapper.toProductDTO(savedProduct);
    }

    public void addReviewToProduct(ProductDTO productDTO, ReviewDTO reviewDTO) {
        Optional<Product> optionalProduct = productRepository.findById(productDTO.id());

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            Review review = reviewMapper.toReview(reviewDTO);
            product.addReview(review);
            productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with id " + productDTO.id());
        }
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product updatedProduct = optionalProduct.get();
            updatedProduct.setName(productDTO.name());
            updatedProduct.setPrice(productDTO.price());
            updatedProduct.setStock(productDTO.stock());
            updatedProduct.setDescription(productDTO.description());
            updatedProduct.setType(productDTO.type());
            updatedProduct.setReviews(productDTO.reviews());
            productRepository.save(updatedProduct);
            return productMapper.toProductDTO(updatedProduct);
        } else {
            throw new RuntimeException("Product not found with id " + id);
        }
    }

    public List<ProductDTO> getAllProducts() {
        return productMapper.toProductsDTO(productRepository.findAll());
    }

    public List<SearchProductDTO> getAllProductsToSearch() {
        return productMapper.toSearchProductsDTO(productRepository.findAll());
    }

    public List<ProductDTO> getProductsByType(String type) {
        return productMapper.toProductsDTO(productRepository.findByType(type));
    }

    public List<SearchProductDTO> getProductsByTypeToSearch(String type) {
        return productMapper.toSearchProductsDTO(productRepository.findByType(type));
    }

    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id).map(productMapper::toProductDTO);
    }

    public ProductDTO updateProduct(Long id, String name, Double price) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(name);
            product.setPrice(price);
            product.setIsAccepted(true);
            productRepository.save(product);
            return productMapper.toProductDTO(product);
        } else {
            throw new RuntimeException("Product not found with id " + id);
        }
    }

    public ProductDTO deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();

        // As books are related to shops, it is needed to load the book shops
        // before deleting it to avoid LazyInitializationException
        ProductDTO productDTO = productMapper.toProductDTO(product);
        productRepository.deleteById(id);

        return productDTO;
    }

    public void setViews_product_count(ProductDTO productDTO) {
        Product product = productMapper.toProduct(productDTO);
        product.setViews_count(product.getViews_count() + 1);
        productRepository.save(product);
    }

    public List<ProductDTO> getAcceptedProductsByType(String type) {
        return productMapper.toProductsDTO(productRepository.findByIsAcceptedTrueAndType(type));
    }

    public Page<ProductDTO> getAcceptedProductsByType(String type, Pageable pageable) {
        Page<Product> productsPage = productRepository.findByIsAcceptedTrueAndType(type, pageable);
        return productsPage.map(productMapper::toProductDTO);
    }

    public List<ProductDTO> getAcceptedProducts() {
        return productMapper.toProductsDTO(productRepository.findByIsAcceptedTrue());
    }

    public Page<ProductDTO> getAcceptedProducts(Pageable pageable) {
        Page<Product> productsPage = productRepository.findByIsAcceptedTrue(pageable);
        return productsPage.map(productMapper::toProductDTO);
    }

    public List<ProductDTO> getNotAcceptedProducts() {
        return productMapper.toProductsDTO(productRepository.findByIsAcceptedFalse());

    }

    public List<SearchProductDTO> searchProductsByName(String query) {
        return productMapper
                .toSearchProductsDTO(productRepository.findByNameContainingIgnoreCaseAndIsAcceptedTrue(query));
    }

    public List<SearchProductDTO> searchProductsByNameAndType(String query, String type) {
        return productMapper
                .toSearchProductsDTO(
                        productRepository.findByNameContainingIgnoreCaseAndTypeAndIsAcceptedTrue(query, type));
    }

    public List<ProductDTO> getAcceptedCompanyProducts(String company) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> acceptedCompanyProducts = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getIsAccepted() && product.getCompany().equals(company)) {
                acceptedCompanyProducts.add(product);
            }
        }
        return productMapper.toProductsDTO(acceptedCompanyProducts);

    }

    public Page<ProductDTO> getAcceptedCompanyProducts(String company, Pageable pageable) {
        Page<Product> productsPage = productRepository.findByIsAcceptedTrueAndCompany(company, pageable);
        return productsPage.map(productMapper::toProductDTO);
    }

    public List<CompanyStadsDTO> getCompanyStadistics(String company) {

        Map<String, Integer> dataMap = new HashMap<>();

        // Initialize the dataMap with predefined keys and zero values
        dataMap.put("Technology", 0);
        dataMap.put("Books", 0);
        dataMap.put("Education", 0);
        dataMap.put("Sports", 0);
        dataMap.put("Home", 0);
        dataMap.put("Music", 0);
        dataMap.put("Cinema", 0);
        dataMap.put("Appliances", 0);
        dataMap.put("Others", 0);

        List<Product> companyProducts = productMapper.toProducts(getAcceptedCompanyProducts(company));

        for (Product product : companyProducts) {
            String type = product.getType();
            dataMap.put(type, dataMap.getOrDefault(type, 0) + 1);
        }

        List<CompanyStadsDTO> dataList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            dataList.add(new CompanyStadsDTO(entry.getKey(), entry.getValue()));
        }

        return dataList;

    }

    public List<ProductDTO> getMostViewedProducts(int limit) {
        List<Product> acceptedProducts = productRepository.findByIsAcceptedTrue();

        // order products by visit number (high to low)
        Collections.sort(acceptedProducts, (p1, p2) -> p2.getViews_count().compareTo(p1.getViews_count()));

        // take only the limit number products
        int size = Math.min(limit, acceptedProducts.size());
        return productMapper.toProductsDTO(acceptedProducts.subList(0, size));
    }

    public List<ProductDTO> getLastProducts() {
        int limit = 4;
        List<Product> acceptedProducts = productRepository.findByIsAcceptedTrue();

        // sort accepted products by creation date
        Collections.sort(acceptedProducts, (p1, p2) -> p2.getDate().compareTo(p1.getDate()));
        int size = Math.min(limit, acceptedProducts.size());
        System.out.println("lista de aceptados");
        for (Product product : acceptedProducts) {
            System.out.println("Fecha:" + product.getDate() + ", Nombre: " + product.getName());
        }
        System.out.println("sublista");
        for (Product product : acceptedProducts.subList(0, size)) {
            System.out.println("Fecha: " + product.getDate() + ", Nombre: " + product.getName());
        }
        return productMapper.toProductsDTO(acceptedProducts.subList(0, size));
    }

    public Page<ProductDTO> getProductsPage(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.map(productMapper::toProductDTO);
    }

    public void convertBlobToBase64(List<ProductDTO> products) {
        List<Product> productsList = productMapper.toProducts(products);
        for (Product product : productsList) {
            try {
                Blob imageBlob = product.getImage();
                if (imageBlob != null) {
                    byte[] bytes = imageBlob.getBytes(1, (int) imageBlob.length());
                    String imageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
                    product.setImageBase64(imageBase64);
                } else {
                    // Default image
                    product.setImageBase64("/images/default-product.jpg");
                }
            } catch (Exception e) {
                e.printStackTrace();
                product.setImageBase64("/images/default-product.jpg");
            }
        }
    }

    public void convertBlobToBase64ToSearch(List<SearchProductDTO> products) {
        List<Product> productsList = productMapper.fromSearchToProducts(products);
        for (Product product : productsList) {
            try {
                Blob imageBlob = product.getImage();
                if (imageBlob != null) {
                    byte[] bytes = imageBlob.getBytes(1, (int) imageBlob.length());
                    String imageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
                    product.setImageBase64(imageBase64);
                } else {
                    // Default image
                    product.setImageBase64("/images/default-product.jpg");
                }
            } catch (Exception e) {
                e.printStackTrace();
                product.setImageBase64("/images/default-product.jpg");
            }
        }
    }

    public Blob getImageById(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            return product.getImage();
        }
        return null;
    }

    public List<ProductDTO> addImageDataToProducts(List<ProductDTO> products) {
        List<ProductDTO> productsList = new ArrayList<>();

        for (ProductDTO productDTO : products) {
            if (productDTO.imageBase64() != null) {
                productsList.add(productDTO);
            } else {
                String imageBase64 = null;

                try {
                    Product product = productMapper.toProduct(productDTO);
                    Long id = product.getId();
                    Blob imageBlob = getImageById(id);

                    if (imageBlob != null) {
                        byte[] bytes = imageBlob.getBytes(1, (int) imageBlob.length());
                        imageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
                    } else {
                        logger.info("No image found for product id: " + id);
                    }
                } catch (Exception e) {
                    logger.error("Error converting image to base64 for product id: " + productDTO.id(), e);
                }

                // Always return a ProductDTO with either the converted image or default
                productsList.add(new ProductDTO(
                        productDTO.id(),
                        productDTO.type(),
                        productDTO.name(),
                        productDTO.company(),
                        productDTO.price(),
                        productDTO.description(),
                        productDTO.stock(),
                        productDTO.isAccepted(),
                        productDTO.date(),
                        productDTO.views_count(),
                        productDTO.reviews(),
                        imageBase64));
            }
        }
        return productsList;
    }

    public ProductDTO addImageToASingleProduct(ProductDTO productDTO) {
        if (productDTO.imageBase64() != null) {
            return productDTO;
        } else {
            String imageBase64 = null;

            try {
                Product product = productMapper.toProduct(productDTO);
                Long id = product.getId();
                Blob imageBlob = getImageById(id);

                if (imageBlob != null) {
                    byte[] bytes = imageBlob.getBytes(1, (int) imageBlob.length());
                    imageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
                } else {
                    logger.info("No image found for product id: " + id);
                }
            } catch (Exception e) {
                logger.error("Error converting image to base64 for product id: " + productDTO.id(), e);
            }

            // Always return a ProductDTO with either the converted image or default
            return new ProductDTO(
                    productDTO.id(),
                    productDTO.type(),
                    productDTO.name(),
                    productDTO.company(),
                    productDTO.price(),
                    productDTO.description(),
                    productDTO.stock(),
                    productDTO.isAccepted(),
                    productDTO.date(),
                    productDTO.views_count(),
                    productDTO.reviews(),
                    imageBase64);
        }
    }

    public List<SearchProductDTO> addImageDataToSearchProducts(List<SearchProductDTO> products) {
        List<SearchProductDTO> productsList = new ArrayList<>();

        for (SearchProductDTO productDTO : products) {
            if (productDTO.imageBase64() != null) {
                productsList.add(productDTO);
            } else {
                String imageBase64 = null;

                try {
                    // Get the product from repository by id
                    Optional<Product> optionalProduct = productRepository.findById(productDTO.id());

                    if (optionalProduct.isPresent()) {
                        Product product = optionalProduct.get();
                        Blob imageBlob = product.getImage();

                        if (imageBlob != null) {
                            byte[] bytes = imageBlob.getBytes(1, (int) imageBlob.length());
                            imageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
                        } else {
                            logger.info("No image found for product id: " + productDTO.id());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error converting image to base64 for product id: " + productDTO.id(), e);
                }

                // Always return a SearchProductDTO with either the converted image or default
                productsList.add(new SearchProductDTO(
                        productDTO.id(),
                        productDTO.name(),
                        productDTO.price(),
                        productDTO.type(),
                        imageBase64));
            }
        }
        return productsList;
    }

    public void updateProductDetails(ProductDTO productDTO, String name, String description, String type, Integer stock,
            Double price, MultipartFile image) {

        Product product = productMapper.toProduct(productDTO);
        product.setName(name);
        product.setDescription(description);
        product.setType(type);
        product.setStock(stock);
        product.setPrice(price);

        // update the image if it is not null
        if (image != null && !image.isEmpty()) {
            try {
                product.setImage(BlobProxy.generateProxy(
                        image.getInputStream(),
                        image.getSize()));
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception as needed
            }
        }

        addProduct(productMapper.toProductDTO(product), product.getCompany());
    }

    public Resource getProductImage(long id) throws SQLException {

        Product product = productRepository.findById(id).orElseThrow();

        if (product.getImage() != null) {
            return new InputStreamResource(product.getImage().getBinaryStream());
        }

        return null;
    }

    public void createProductImage(long id, InputStream inputStream, long size) {

        Product product = productRepository.findById(id).orElseThrow();

        product.setImage(BlobProxy.generateProxy(inputStream, size));

        productRepository.save(product);
    }

    public void replaceProductImage(long id, InputStream inputStream, long size) {

        Product product = productRepository.findById(id).orElseThrow();

        if (product.getImage() == null) {
            throw new NoSuchElementException();
        }

        product.setImage(BlobProxy.generateProxy(inputStream, size));

        productRepository.save(product);
    }

    public void deleteProductImage(long id) {

        Product product = productRepository.findById(id).orElseThrow();

        if (product.getImage() == null) {
            throw new NoSuchElementException();
        }

        product.setImage(null);

        productRepository.save(product);
    }

}