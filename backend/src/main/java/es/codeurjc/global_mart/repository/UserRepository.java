package es.codeurjc.global_mart.repository;

import es.codeurjc.global_mart.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Busca un usuario por nombre de usuario

    Optional<User> findByEmail(String email); // Busca un usuario por email

    // Add this method
    List<User> findAllByUsername(String username);

}
