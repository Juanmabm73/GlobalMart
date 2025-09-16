package es.codeurjc.global_mart.dto.User;

import java.util.List;

import es.codeurjc.global_mart.dto.Orders.OrderDTO;
import es.codeurjc.global_mart.dto.Product.ProductDTO;
import es.codeurjc.global_mart.dto.Reviewss.ReviewDTO;

public record UserCreationDTO(
        String name,
        String username,
        String email,
        String password,
        List<String> role,
        List<OrderDTO> orders,
        List<ReviewDTO> reviews,
        List<ProductDTO> cart,
        Double cartPrice,
        List<Double> historicalOrderPrices,
        String imageBase64) {
}
