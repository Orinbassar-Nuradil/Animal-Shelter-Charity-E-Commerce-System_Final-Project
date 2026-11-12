package com.pawhaven.patterns.observer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** Registers the available observer beans and sends each a synchronous stock-change notification. */
@Component
public class StockEventPublisher {
    private final CopyOnWriteArrayList<InventoryObserver> observers = new CopyOnWriteArrayList<>();

    public StockEventPublisher(List<InventoryObserver> subscribers) {
        subscribers.forEach(this::register);
    }

    public void register(InventoryObserver observer) { observers.addIfAbsent(Objects.requireNonNull(observer)); }
    public void unregister(InventoryObserver observer) { observers.remove(observer); }

    public void publish(StockChange change) {
        observers.forEach(observer -> observer.onStockChanged(change));
    }
}
