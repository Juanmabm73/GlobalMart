package es.codeurjc.global_mart.dto.Product;

import java.util.List;
import java.util.Date;

import es.codeurjc.global_mart.model.Review;

public record ProductDTO(
        Long id,
        String type,
        String name,
        String company,
        Double price,
        String description,
        Integer stock,
        Boolean isAccepted,
        Date date,
        Integer views_count,
        List<Review> reviews,
        String imageBase64) {
}
