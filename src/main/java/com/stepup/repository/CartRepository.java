package com.stepup.repository;

import com.stepup.model.Cart;
import com.stepup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findByUser(User user);
}
