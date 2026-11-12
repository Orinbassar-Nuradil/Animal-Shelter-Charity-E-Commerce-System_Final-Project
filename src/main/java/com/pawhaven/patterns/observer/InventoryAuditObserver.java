package com.pawhaven.patterns.observer;

import com.pawhaven.commerce.InventoryRecord;
import com.pawhaven.commerce.InventoryRecordRepository;
import org.springframework.stereotype.Component;

@Component
public class InventoryAuditObserver implements InventoryObserver {
    private final InventoryRecordRepository history;

    public InventoryAuditObserver(InventoryRecordRepository history) { this.history = history; }

    public void onStockChanged(StockChange change) {
        history.save(new InventoryRecord(InventoryRecord.Kind.AUDIT, change));
    }
}
