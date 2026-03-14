package Girolook.com.GiroLook.repository;

import Girolook.com.GiroLook.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p JOIN FETCH p.store WHERE p.active = true")
    List<Product> findAllActiveWithStore();
}
