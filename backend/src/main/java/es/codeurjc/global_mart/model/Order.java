package es.codeurjc.global_mart.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.List;
import java.sql.Date;

@Entity
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double total; // Total price of the order

    private String direction; // Direction where the order will be sent

    private String userName;

    @ManyToOne(cascade = CascadeType.ALL)
    private User user; // Our order have one user

    @OneToMany
    @JoinColumn(name = "order_id")
    private List<Product> products; // Our order have minum one product to n products

    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    // ----------------- Constructor -----------------
    public Order(String username, Double total, User user, List<Product> cart) {
        this.date = new Date(System.currentTimeMillis()); // yyyy/mm/dd
        this.userName = username;
        this.total = total;
        this.user = user;
        this.products = cart;

    }

    public Order() {

    }

    // ----------------- Methods -----------------

    public void addProduct(Product product) { // Add a product to the order and actualize the total price
        products.add(product);
        total += product.getPrice();
    }

    public void deleteProduct(Product product) { // Delete a product from the order and actualize the total price
        products.remove(product);
        total -= product.getPrice();
    }

    // !Getters
    public Long getId() {
        return id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Double getTotal() {
        return total;
    }

    public String getDirection() {
        return direction;
    }

    public String getUserName() {
        return userName;
    }

    // !Setters
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
