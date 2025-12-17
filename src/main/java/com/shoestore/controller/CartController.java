package com.shoestore.controller;

import com.shoestore.entity.CartItem;
import com.shoestore.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Получить корзину пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCart(@PathVariable Long userId) {
        try {
            var cart = cartService.getOrCreateUserCart(userId);
            List<CartItem> items = cartService.getCartItems(cart.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("cartId", cart.getId());
            response.put("items", items);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Получить корзину сессии (для неавторизованных)
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionCart(@PathVariable String sessionId) {
        try {
            var cart = cartService.getOrCreateSessionCart(sessionId);
            List<CartItem> items = cartService.getCartItems(cart.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("cartId", cart.getId());
            response.put("items", items);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Добавить товар в корзину
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        try {
            Long cartId = Long.parseLong(request.get("cartId").toString());
            Long productId = Long.parseLong(request.get("productId").toString());
            Long sizeId = Long.parseLong(request.get("sizeId").toString());
            Integer quantity = Integer.parseInt(request.get("quantity").toString());

            CartItem item = cartService.addToCart(cartId, productId, sizeId, quantity);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Обновить количество товара
    @PutMapping("/item/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long itemId,
                                            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            CartItem item = cartService.updateCartItemQuantity(itemId, quantity);

            if (item == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Товар удален из корзины");
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(item);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Удалить товар из корзины
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId) {
        try {
            cartService.removeFromCart(itemId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Товар удален из корзины");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Очистить корзину
    @DeleteMapping("/clear/{cartId}")
    public ResponseEntity<?> clearCart(@PathVariable Long cartId) {
        try {
            cartService.clearCart(cartId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Корзина очищена");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}