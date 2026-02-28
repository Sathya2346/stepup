package com.stepup.repository;

import com.stepup.model.ReturnRequest;
import com.stepup.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findByOrder(Order order);
}
