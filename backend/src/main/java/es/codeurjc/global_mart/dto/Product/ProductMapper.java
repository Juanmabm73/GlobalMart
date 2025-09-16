package es.codeurjc.global_mart.dto.Product;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;

import es.codeurjc.global_mart.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toProductDTO(Product product);

    List<ProductDTO> toProductsDTO(Collection<Product> products);

    Product toProduct(ProductDTO productDTO);

    List<Product> toProducts(List<ProductDTO> productDTOs);

    SearchProductDTO toSearchProductDTO(Product product);

    List<SearchProductDTO> toSearchProductsDTO(List<Product> products);

    Product fromSearchToProduct(SearchProductDTO searchProductDTO);

    List<Product> fromSearchToProducts(List<SearchProductDTO> searchProductDTOs);

}