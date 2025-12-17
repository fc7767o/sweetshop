package com.shoestore.service;

import com.shoestore.entity.Product;
import com.shoestore.entity.ProductSize;
import com.shoestore.repository.ProductRepository;
import com.shoestore.repository.ProductSizeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    public ProductService(ProductRepository productRepository,
                          ProductSizeRepository productSizeRepository) {
        this.productRepository = productRepository;
        this.productSizeRepository = productSizeRepository;
    }

    // Получить все активные товары
    public List<Product> getAllActiveProducts() {
        return productRepository.findByStatus(Product.Status.ACTIVE);
    }

    // Получить товары по категории
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // Получить товары по бренду
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    // Создать новый товар
    public Product createProduct(String name, BigDecimal price, String category,
                                 String brand, String description) {
        Product product = new Product(name, price, category);
        product.setBrand(brand);
        product.setDescription(description);
        return productRepository.save(product);
    }

    // Добавить размер к товару
    public ProductSize addSizeToProduct(Long productId, BigDecimal size, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        ProductSize productSize = new ProductSize();
        productSize.setProduct(product);
        productSize.setSize(size);
        productSize.setQuantity(quantity);

        return productSizeRepository.save(productSize);
    }

    // Обновить количество товара
    public void updateProductQuantity(Long sizeId, Integer newQuantity) {
        ProductSize size = productSizeRepository.findById(sizeId)
                .orElseThrow(() -> new RuntimeException("Размер не найден"));

        size.setQuantity(newQuantity);
        productSizeRepository.save(size);
    }
}