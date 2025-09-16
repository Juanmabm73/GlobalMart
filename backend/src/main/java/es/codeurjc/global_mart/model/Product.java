package es.codeurjc.global_mart.model;

import java.sql.Timestamp;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "PRODUCTS")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String name;
    private String company;
    private Double price;
    private String description;
    private Integer views_count;

    @JsonIgnore
    @Lob
    private Blob image;

    private Integer stock;
    private Boolean isAccepted;

    @Column(columnDefinition = "LONGTEXT")
    private String imageBase64;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Review> reviews;

    private Timestamp date;

    // ----------------- Constructor -----------------
    public Product() {
    }

    public Product(String type, String name, String company, Double price, String description, Blob image,
            Integer stock,
            Boolean isAccepted) {
        this.type = type;
        this.name = name;
        this.company = company;
        this.price = price;
        this.description = description;
        this.image = image;
        this.stock = stock;
        this.reviews = new ArrayList<>();
        this.isAccepted = isAccepted;
        this.views_count = 0;
        this.date = new Timestamp(System.currentTimeMillis());
    }

    // ----------------- Methods -----------------
    // !Getters
    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Blob getImage() {
        return image;
    }

    public Integer getStock() {
        return stock;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public Timestamp getDate() {
        return date;
    }
    public Integer getViewsCount() {
        return views_count;
    }

    // !Setters
    public void setViewsCount(Integer views_count) {
        this.views_count = views_count;
    }
    public void setIsAccepted(Boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public Integer getViews_count() {
        return views_count;
    }

    public void setViews_count(Integer views_count) {
        this.views_count = views_count;
    }
}
