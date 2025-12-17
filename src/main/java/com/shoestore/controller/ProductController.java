package com.shoestore.controller;

import com.shoestore.entity.Product;
import com.shoestore.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Получить все активные товары
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(products);
    }

    // Получить товары по категории
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    // Получить товары по бренду
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Product>> getProductsByBrand(@PathVariable String brand) {
        List<Product> products = productService.getProductsByBrand(brand);
        return ResponseEntity.ok(products);
    }

    // Создать новый товар (для админа/менеджера)
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            BigDecimal price = new BigDecimal(request.get("price").toString());
            String category = (String) request.get("category");
            String brand = (String) request.get("brand");
            String description = (String) request.get("description");

            Product product = productService.createProduct(name, price, category, brand, description);

            // Добавить размеры если указаны
            if (request.containsKey("sizes")) {
                List<Map<String, Object>> sizes = (List<Map<String, Object>>) request.get("sizes");
                for (Map<String, Object> sizeData : sizes) {
                    BigDecimal size = new BigDecimal(sizeData.get("size").toString());
                    Integer quantity = Integer.parseInt(sizeData.get("quantity").toString());
                    productService.addSizeToProduct(product.getId(), size, quantity);
                }
            }

            return ResponseEntity.ok(product);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка создания товара: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Добавить размер к товару
    @PostMapping("/{productId}/sizes")
    public ResponseEntity<?> addSize(@PathVariable Long productId,
                                     @RequestBody Map<String, Object> request) {
        try {
            BigDecimal size = new BigDecimal(request.get("size").toString());
            Integer quantity = Integer.parseInt(request.get("quantity").toString());

            var result = productService.addSizeToProduct(productId, size, quantity);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}