package es.codeurjc.global_mart.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "REVIEWS")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private String username;
    private String comment;
    private int calification; // Por ejemplo, de 1 a 5
    private LocalDateTime creationDate;

    public Review() {}

    public Review(String username, String comment, int calification) {
        if (calificationValidation(calification)) { 
            this.username = username;  
            this.comment = comment;
            this.calification = calification;
            this.creationDate = LocalDateTime.now();
        }else{
            throw new IllegalArgumentException("Calification must be between 0 and 5");
        }
    }

    // Getters and Setters
    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long id) {
        this.reviewId = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getCalification() {
        return calification;
    }

    public void setCalification(int calification) {
        if (calificationValidation(calification)) {
            this.calification = calification;
        } else {
            throw new IllegalArgumentException("Calification must be between 0 and 5");
        }
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean calificationValidation(int calification) {
        return calification >= 0 && calification <= 5;
    }
}