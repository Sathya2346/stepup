package com.stepup.controller;

import com.stepup.model.Order;
import com.stepup.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderLocation(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", order.getStatus());
        response.put("lat", order.getCurrentLatitude());
        response.put("lng", order.getCurrentLongitude());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/simulate")
    public ResponseEntity<String> simulateMovement(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order != null && order.getStatus() == Order.OrderStatus.OUT_FOR_DELIVERY) {
            // Slight random movement for simulation
            double lat = order.getCurrentLatitude() != null ? order.getCurrentLatitude() : 28.6139;
            double lng = order.getCurrentLongitude() != null ? order.getCurrentLongitude() : 77.2090;
            
            order.setCurrentLatitude(lat + (Math.random() - 0.5) * 0.001);
            order.setCurrentLongitude(lng + (Math.random() - 0.5) * 0.001);
            
            orderService.saveOrder(order);
            return ResponseEntity.ok("Simulated movement");
        }
        return ResponseEntity.badRequest().body("Order not in OUT_FOR_DELIVERY status");
    }
}
