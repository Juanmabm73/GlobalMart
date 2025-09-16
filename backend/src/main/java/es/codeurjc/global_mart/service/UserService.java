package es.codeurjc.global_mart.service;

import es.codeurjc.global_mart.repository.UserRepository;
import es.codeurjc.global_mart.model.User;
import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.Product.ProductMapper;
import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.dto.User.HistoricalOrdersDTO;
import es.codeurjc.global_mart.dto.User.ShoppingCartDTO;
import es.codeurjc.global_mart.dto.User.UserCartPriceDTO;
import es.codeurjc.global_mart.dto.User.UserMapper;
import es.codeurjc.global_mart.model.Product;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    public User createUser(MultipartFile image, String name, String username, String email, String password,
            List<String> role) throws IOException {
        User user = new User(name, username, email, password, role);
        if (image != null && !image.isEmpty()) {
            user.setImage(BlobProxy.generateProxy(image.getInputStream(), image.getSize()));
        } else {
            user.setImage(BlobProxy.generateProxy(
                    "https://www.pngitem.com/pimgs/m/146-1468479_my-profile-icon-blank-profile-picture-circle-hd.png"
                            .getBytes()));
        }
        return userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        return userMapper.toUsersDTO(userRepository.findAll());
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toUserDTO); // transform an optional user into an optional
                                                                       // userdto
    }

    public UserDTO updateUser(Long id, String username, String email, String password) {
        Optional<UserDTO> optionalUser = getUserById(id);
        if (optionalUser.isPresent()) {
            User user = userMapper.toUser(optionalUser.get()); // from DTO tu entity
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return userMapper.toUserDTO(user);
        } else {
            throw new RuntimeException("User not found with id " + id);
        }
    }

    public Optional<UserDTO> findByUsername(String username) {
        List<User> users = userRepository.findAllByUsername(username);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        // Use the first user found
        return Optional.of(userMapper.toUserDTO(users.get(0)));
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public ShoppingCartDTO getShoppingCartData(UserDTO user) {
        User u = userMapper.toUser(user);
        List<ProductDTO> cartProducts = productMapper.toProductsDTO(u.getCart());
        Double price = u.getCartPrice();
        return new ShoppingCartDTO(cartProducts, price);
    }

    public UserDTO addImageBase64ToUser(UserDTO userDTO) {
        User user = userMapper.toUser(userDTO);
        // convert Blob to Base64 encoded string
        try {
            Blob imageBlob = user.getImage();
            if (imageBlob != null) {
                byte[] bytes = imageBlob.getBytes(1, (int) imageBlob.length());
                String imageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
                user.setImageBase64(imageBase64);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userMapper.toUserDTO(user);
    }

    public void addProductToCart(UserDTO userDTO, ProductDTO productDTO) {
        // Obtener el usuario completo de la base de datos
        User user = userRepository.findByUsername(userDTO.username())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userDTO.username()));

        // Obtener el producto completo
        Product product = productMapper.toProduct(productDTO);

        // Asegurarse de que el carrito está inicializado
        if (user.getCart() == null) {
            user.setCart(new ArrayList<>());
        }

        // Añadir el producto al carrito
        user.addProductToCart(product);

        // Guardar el usuario actualizado
        userRepository.save(user);
    }

    public boolean productInCart(UserDTO userDTO, ProductDTO productDTO) {
        User user = userRepository.findByUsername(userDTO.username())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userDTO.username()));

        // Search by product ID rather than using contains()
        if (user.getCart() != null) {
            for (Product p : user.getCart()) {
                if (p.getId().equals(productDTO.id())) {
                    return true;
                }
            }
        }

        return false;
    }

    public void removeProductFromCart(UserDTO userDTO, ProductDTO productDTO) {
        try {
            // Get existing user from database by username
            User user = userRepository.findByUsername(userDTO.username())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + userDTO.username()));

            System.out.println("Removing product for user: " + user.getUsername() + " (ID: " + user.getId() + ")");

            // Get the product
            Product product = productMapper.toProduct(productDTO);

            // Find matching product in cart by ID
            Product productToRemove = null;
            if (user.getCart() != null) {
                for (Product p : user.getCart()) {
                    if (p.getId().equals(product.getId())) {
                        productToRemove = p;
                        break;
                    }
                }
            }

            // Remove product if found
            if (productToRemove != null) {
                // Get price before removing for subtraction
                double productPrice = productToRemove.getPrice();

                // Remove product
                user.getCart().remove(productToRemove);

                // Update cart price
                user.setCartPrice(user.getCartPrice() - productPrice);

                System.out.println("Product removed successfully. New cart size: " + user.getCart().size());
                System.out.println("New cart total: " + user.getCartPrice());

                // Save changes
                userRepository.save(user);
            } else {
                System.out.println("Product not found in cart");
            }
        } catch (Exception e) {
            System.err.println("Error removing product from cart: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public UserCartPriceDTO getTotalPrice(User user) {
        return userMapper.toCartPriceDTO(user);
    }

    public void restartCart(User user) {
        user.emptyCart();
        userRepository.save(user);
    }

    public Optional<UserDTO> findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(userMapper::toUserDTO);
    }

    public HistoricalOrdersDTO getUserStads(String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new RuntimeException("User not found with username " + name));

        List<Double> list = user.getHistoricalOrderPrices();
        System.out.println("Order prices list" + list);

        HistoricalOrdersDTO orders = new HistoricalOrdersDTO(list);
        return orders;
    }

    public boolean isAdmin(UserDTO userDTO) {
        User user = userMapper.toUser(userDTO);
        return user.isAdmin();
    }

    public boolean isCompany(UserDTO userDTO) {
        User user = userMapper.toUser(userDTO);
        return user.isCompany();
    }

    public boolean isUser(UserDTO userDTO) {
        User user = userMapper.toUser(userDTO);
        return user.isUser();
    }

    public void convertBlobToBase64(ProductDTO productDTO) {
        Product product = productMapper.toProduct(productDTO);
        // convert Blob to Base64 encoded string
        try {
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

    public Resource getUserImage(long id) throws SQLException {

        User user = userRepository.findById(id).orElseThrow();

        if (user.getImage() != null) {
            return new InputStreamResource(user.getImage().getBinaryStream());
        } else {
            throw new NoSuchElementException();
        }
    }

    public Long getUserId(UserDTO userDTO) {
        User user = userMapper.toUser(userDTO);
        return user.getId();
    }

    public void createUserImage(long id, InputStream inputStream, long size) {

        User user = userRepository.findById(id).orElseThrow();

        user.setImage(BlobProxy.generateProxy(inputStream, size));

        userRepository.save(user);
    }

    public void replaceUserImage(long id, InputStream inputStream, long size) {

        User user = userRepository.findById(id).orElseThrow();

        if (user.getImage() == null) {
            throw new NoSuchElementException();
        }

        user.setImage(BlobProxy.generateProxy(inputStream, size));

        userRepository.save(user);
    }

    public void deleteUserImage(long id) {

        User user = userRepository.findById(id).orElseThrow();

        if (user.getImage() == null) {
            throw new NoSuchElementException();
        }

        user.setImage(null);

        userRepository.save(user);
    }

}