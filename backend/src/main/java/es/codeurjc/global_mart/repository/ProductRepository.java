package es.codeurjc.global_mart.repository;

import es.codeurjc.global_mart.model.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByType(String type);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCaseAndType(String name, String type);

    List<Product> findByNameContainingIgnoreCaseAndIsAcceptedTrue(String name);

    List<Product> findByNameContainingIgnoreCaseAndTypeAndIsAcceptedTrue(String name, String type);

    List<Product> findByIsAcceptedTrue();

    List<Product> findByIsAcceptedFalse();

    List<Product> findByIsAcceptedTrueAndType(String type);

    Page<Product> findByIsAcceptedFalse(Pageable pageable);

    Page<Product> findByIsAcceptedTrue(Pageable pageable);

    Page<Product> findByIsAcceptedTrueAndType(String type, Pageable pageable);

    Page<Product> findByIsAcceptedTrueAndCompany(String company, Pageable pageable);

}
