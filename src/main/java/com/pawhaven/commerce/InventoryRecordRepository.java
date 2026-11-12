package com.pawhaven.commerce;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRecordRepository extends JpaRepository<InventoryRecord, Long> {
    List<InventoryRecord> findTop100ByOrderByIdDesc();
}
