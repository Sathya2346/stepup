package com.stepup.service;

import com.stepup.model.*;
import com.stepup.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private CartService cartService;

    @Transactional
    public Order placeOrder(User user, String address, String paymentMethod, String razorpayOrderId,
            String paymentId) {
        // Fetch managed user to avoid detached entity issues
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.getCart(managedUser);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create a copy of items because we will clear the cart
        List<CartItem> cartItems = new ArrayList<>(cart.getItems());

        Order order = new Order();
        order.setUser(managedUser);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(address);

        if ("ONLINE".equals(paymentMethod)) {
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentStatus("PAID");
            order.setRazorpayOrderId(razorpayOrderId);
            order.setPaymentId(paymentId);
        } else {
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentStatus("COD");
        }

        // Calculate Discount logic
        BigDecimal rawSubtotal = cartItems.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(cart.getTotalAmount());

        if (cart.getAppliedCoupon() != null && cart.getAppliedCoupon().isActive()) {
            order.setCouponCode(cart.getAppliedCoupon().getCode());
            order.setDiscountAmount(rawSubtotal.subtract(cart.getTotalAmount()));
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setCouponCode(null);
        }

        order.setTrackingNumber(java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setExpectedDeliveryDate(LocalDate.now().plusDays(5));
        order.setRefundStatus("NOT_APPLICABLE");

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            String selectedSize = cartItem.getSize();

            // Refedetch product to get latest stock state inside transaction
            Product latestProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + product.getName()));

            String keyToUse = selectedSize;
            if (selectedSize == null || latestProduct.getSizes() == null) {
                throw new RuntimeException("Invalid size information for product " + latestProduct.getName());
            }

            if (!latestProduct.getSizes().containsKey(keyToUse)) {
                // Case insensitive fallback
                String foundKey = null;
                for (String key : latestProduct.getSizes().keySet()) {
                    if (key.trim().equalsIgnoreCase(selectedSize.trim())) {
                        foundKey = key;
                        break;
                    }
                }
                if (foundKey != null) {
                    keyToUse = foundKey;
                } else {
                    throw new RuntimeException("Size " + selectedSize + " not found for " + latestProduct.getName());
                }
            }

            Integer availableQuantity = latestProduct.getSizes().get(keyToUse);
            if (availableQuantity == null || availableQuantity < cartItem.getQuantity()) {
                throw new RuntimeException("Stock sold out for size " + keyToUse + " of " + latestProduct.getName());
            }

            // Decrease quantity
            int newQuantity = availableQuantity - cartItem.getQuantity();
            latestProduct.getSizes().put(keyToUse, newQuantity);

            // Update inStock status if total stock is 0 or less
            if (latestProduct.getTotalStock() <= 0) {
                latestProduct.setInStock(false);
            }
            productRepository.save(latestProduct);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(latestProduct);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSize(cartItem.getSize());
            orderItem.setPrice(latestProduct.getPrice());
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(managedUser);

        return savedOrder;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updatePayment(Long orderId, String razorpayOrderId, String paymentId, String status) {
        Order order = getOrderById(orderId);
        if (order != null) {
            order.setRazorpayOrderId(razorpayOrderId);
            order.setPaymentId(paymentId);
            order.setPaymentStatus(status);
            orderRepository.save(order);
        }
    }

    @Transactional
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        if (order != null
                && (order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.SHIPPED)) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setCancelReason(reason);
            if ("PAID".equals(order.getPaymentStatus())) {
                order.setRefundStatus("PENDING");
            }
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order cannot be cancelled at this stage.");
        }
    }

    @Transactional
    public void requestReturn(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        if (order != null && order.getStatus() == Order.OrderStatus.DELIVERED) {
            order.setStatus(Order.OrderStatus.RETURN_REQUESTED);
            order.setReturnReason(reason);

            ReturnRequest request = new ReturnRequest();
            request.setOrder(order);
            request.setReason(reason);
            returnRequestRepository.save(request);

            orderRepository.save(order);
        } else {
            throw new RuntimeException("Only delivered orders can be returned.");
        }
    }

    @Transactional
    public void createSupportTicket(User user, String subject, String description) {
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        supportTicketRepository.save(ticket);
    }
}
