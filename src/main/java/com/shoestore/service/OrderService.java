package com.shoestore.service;

import com.shoestore.entity.*;
import com.shoestore.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ProductSizeRepository productSizeRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartService cartService,
                        ProductSizeRepository productSizeRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.productSizeRepository = productSizeRepository;
    }

    // Создать заказ из корзины
    public Order createOrderFromCart(Long userId, String shippingAddress, String paymentMethod) {
        // Получить корзину пользователя
        Cart cart = cartService.getOrCreateUserCart(userId);
        List<CartItem> cartItems = cartService.getCartItems(cart.getId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }

        // Проверить наличие товаров на складе и рассчитать сумму
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            ProductSize size = item.getSize();
            if (size.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Недостаточно товара: " + item.getProduct().getName() +
                        " размер " + size.getSize());
            }
            totalAmount = totalAmount.add(item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Создать заказ
        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(Order.Status.CREATED);
        Order savedOrder = orderRepository.save(order);

        // Создать элементы заказа и обновить остатки
        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(item.getProduct());
            orderItem.setSize(item.getSize());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getProduct().getPrice());
            orderItemRepository.save(orderItem);

            // Обновить остатки на складе
            ProductSize size = item.getSize();
            size.setQuantity(size.getQuantity() - item.getQuantity());
            productSizeRepository.save(size);
        }

        // Очистить корзину
        cartService.clearCart(cart.getId());

        return savedOrder;
    }

    // Получить заказы пользователя
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // Обновить статус заказа
    public Order updateOrderStatus(Long orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus(status);
        return orderRepository.save(order);
    }

    // Получить все заказы (для админа)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Получить элементы заказа
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}