package com.pawhaven.commerce;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelterOrderRepository extends JpaRepository<ShelterOrder, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<ShelterOrder> findByReceiptCode(UUID receiptCode);

    @EntityGraph(attributePaths = "items")
    List<ShelterOrder> findAllByOrderByIdDesc();
}
