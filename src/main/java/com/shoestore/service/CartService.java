package com.shoestore.service;

import com.shoestore.entity.*;
import com.shoestore.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       ProductSizeRepository productSizeRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productSizeRepository = productSizeRepository;
    }

    // Получить или создать корзину для пользователя
    public Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = new User();
                    user.setId(userId);

                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    // Получить или создать корзину для сессии (неавторизованный пользователь)
    public Cart getOrCreateSessionCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setSessionId(sessionId);
                    return cartRepository.save(cart);
                });
    }

    // Добавить товар в корзину
    public CartItem addToCart(Long cartId, Long productId, Long sizeId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        ProductSize size = productSizeRepository.findById(sizeId)
                .orElseThrow(() -> new RuntimeException("Размер не найден"));

        // Проверить, есть ли уже такой товар в корзине
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductIdAndSizeId(cartId, productId, sizeId);

        if (existingItem.isPresent()) {
            // Увеличить количество
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            // Добавить новый товар
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setSize(size);
            newItem.setQuantity(quantity);
            return cartItemRepository.save(newItem);
        }
    }

    // Получить все товары в корзине
    public List<CartItem> getCartItems(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    // Обновить количество товара в корзине
    public CartItem updateCartItemQuantity(Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Товар в корзине не найден"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    // Удалить товар из корзины
    public void removeFromCart(Long itemId) {
        cartItemRepository.deleteById(itemId);
    }

    // Очистить корзину
    public void clearCart(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        cartItemRepository.deleteAll(items);
    }
}