package com.stepup.service;

import com.stepup.model.Cart;
import com.stepup.model.CartItem;
import com.stepup.model.Product;
import com.stepup.model.User;

import com.stepup.repository.CartItemRepository;
import com.stepup.repository.CartRepository;
import com.stepup.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Cart getCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        // Force initialization if needed, but @Transactional should handle it
        // if called from within the same context.
        return cart;
    }

    @Transactional
    public Integer getCartQuantity(User user) {
        if (user == null)
            return 0;
        Cart cart = getCart(user);
        return cart.getTotalQuantity();
    }

    @Transactional
    public void addToCart(User user, Long productId, int quantity, String size) {
        Cart cart = getCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId) &&
                        (size == null ? item.getSize() == null : size.equalsIgnoreCase(item.getSize())))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setSize(size);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getCart(user);
        // Delete all cart items first
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setAppliedCoupon(null);
        cartRepository.save(cart);
    }

    @Transactional
    public void updateQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateSize(Long cartItemId, String size) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setSize(size);
        cartItemRepository.save(item);
    }

    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }
}
