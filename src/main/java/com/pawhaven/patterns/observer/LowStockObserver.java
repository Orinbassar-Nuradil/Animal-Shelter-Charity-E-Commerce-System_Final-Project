package com.pawhaven.patterns.observer;

import com.pawhaven.commerce.InventoryRecord;
import com.pawhaven.commerce.InventoryRecordRepository;
import org.springframework.stereotype.Component;

@Component
public class LowStockObserver implements InventoryObserver {
    public static final int THRESHOLD = 3;
    private final InventoryRecordRepository history;

    public LowStockObserver(InventoryRecordRepository history) { this.history = history; }

    public void onStockChanged(StockChange change) {
        if (change.previousStock() > THRESHOLD && change.newStock() <= THRESHOLD) {
            history.save(new InventoryRecord(InventoryRecord.Kind.LOW_STOCK, change));
        }
    }
}
