package com.pawhaven.patterns.observer;

/** Reacts to a stock change inside the same transaction as the inventory write. */
public interface InventoryObserver {
    void onStockChanged(StockChange change);
}
