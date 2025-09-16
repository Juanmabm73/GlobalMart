package es.codeurjc.global_mart.dto.User;

import java.util.List;

public record HistoricalOrdersDTO(
    List<Double> historicalOrdersPrices
) {
    
}
