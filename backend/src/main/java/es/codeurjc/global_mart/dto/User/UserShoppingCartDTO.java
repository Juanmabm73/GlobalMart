package es.codeurjc.global_mart.dto.User;

import java.util.List;

import es.codeurjc.global_mart.dto.Product.ProductDTO;

public record UserShoppingCartDTO(
        String username,
        List<ProductDTO> cart,
        Double cartPrice) {

}
