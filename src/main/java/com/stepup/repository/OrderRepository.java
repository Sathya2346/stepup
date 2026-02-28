package com.stepup.repository;

import com.stepup.model.Order;
import com.stepup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @EntityGraph(attributePaths = {"items"})
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    @EntityGraph(attributePaths = {"items"})
    List<Order> findAll();
}
