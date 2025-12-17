package com.shoestore.repository;

import com.shoestore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(Product.Status status);
    List<Product> findByCategory(String category);
    List<Product> findByBrand(String brand);
}