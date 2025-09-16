package es.codeurjc.global_mart.dto.Product;

public record SearchProductDTO(
                Long id,
                String name,
                Double price,
                String type,
                String imageBase64) {
}