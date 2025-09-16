package es.codeurjc.global_mart.dto.Orders;

import java.util.List;

import es.codeurjc.global_mart.dto.Product.ProductDTO;

public record OrderDTO(
    Long id,
    Double total,
    List<ProductDTO> products,
    String userName) {
}
