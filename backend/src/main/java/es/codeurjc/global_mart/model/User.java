package es.codeurjc.global_mart.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

import java.util.ArrayList;
import java.util.List;

import java.sql.Blob;

@Entity(name = "Users")
public class User {

    // ----------------- Attributes -----------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private Blob image;

    private String name;

    @Column(unique = true)
    private String username;

    private String email;
    private String encodedPassword;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @OneToMany
    private List<Review> reviews;

    @OneToMany
    private List<Product> cart;

    private double cartPrice;
    private String imageBase64;

    @ElementCollection(fetch = FetchType.EAGER) // EAGER: fetch the data when the object is created
    private List<Double> historicalOrderPrices;

    // ----------------- Constructor -----------------
    public User() {
    }

    public User(String name, String username, String email, String password, List<String> role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.encodedPassword = password;
        this.orders = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.role = role;
        this.cart = new ArrayList<>();
        this.cartPrice = 0;
        this.historicalOrderPrices = new ArrayList<>();

    }

    public boolean isAdmin() {
        return role.contains("ADMIN");
    }

    public boolean isCompany() {
        return role.contains("COMPANY");
    }

    public boolean isUser() {
        return role.contains("USER");
    }

    // ----------------- Methods -----------------
    // !Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public String getPassword() {
        return encodedPassword;
    }

    public List<String> getRole() {
        return role;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public Blob getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public List<Product> getCart() {
        return cart;
    }

    public double getTotalPrice() {
        double totalPrice = 0;
        for (Product product : cart) {
            totalPrice += product.getPrice();
        }
        return totalPrice;
    }

    public List<Double> getHistoricalOrderPrices() {
        return historicalOrderPrices;
    }

    public double getCartPrice() {
        return cartPrice;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    // !Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.encodedPassword = password;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHistoricalOrderPrices(List<Double> historicalOrderPrices) {
        this.historicalOrderPrices = historicalOrderPrices;
    }

    public void setCartPrice(int cartPrice) {
        this.cartPrice = cartPrice;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    // Reviews
    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    // Cart methods

    public void addProductToCart(Product product) {
        this.cart.add(product);
        this.cartPrice += product.getPrice();
    }

    public void removeProductFromCart(Product product) {
        this.cart.remove(product);
        this.cartPrice -= product.getPrice();
    }

    public void emptyCart() {
        this.cart.clear();
        this.cartPrice = 0;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public void setCart(List<Product> cart) {
        this.cart = cart;
    }

    public void setCartPrice(double cartPrice) {
        this.cartPrice = cartPrice;
    }

}
