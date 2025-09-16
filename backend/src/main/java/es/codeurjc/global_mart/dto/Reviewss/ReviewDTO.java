package es.codeurjc.global_mart.dto.Reviewss;

import java.time.LocalDate;

public record ReviewDTO(
        Long reviewId,
        String username,
        String comment,
        int calification,
        LocalDate creationDate) {
}
