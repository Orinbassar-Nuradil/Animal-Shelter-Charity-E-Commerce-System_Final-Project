package com.pawhaven.commerce;

import com.pawhaven.patterns.observer.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockObserverTest {
    @Test
    void publisherRegistersEachObserverOnceAndUnsubscribesIt() {
        var seen = new ArrayList<StockChange>();
        InventoryObserver observer = seen::add;
        var publisher = new StockEventPublisher(List.of(observer));
        publisher.register(observer);
        var change = new StockChange(product(), 4, 3, "Order");
        publisher.publish(change);
        publisher.unregister(observer);
        publisher.publish(change);
        assertThat(seen).containsExactly(change);
    }

    @Test
    void auditsEveryChangeAndAlertsOnlyWhenCrossingIntoLowStock() {
        var history = mock(InventoryRecordRepository.class);
        var publisher = new StockEventPublisher(List.of(new InventoryAuditObserver(history), new LowStockObserver(history)));
        var product = product();
        publisher.publish(new StockChange(product, 4, 3, "Order 1"));
        publisher.publish(new StockChange(product, 3, 1, "Order 2"));
        publisher.publish(new StockChange(product, 1, 8, "Admin stock adjustment"));
        var records = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(history, times(4)).save(records.capture());
        assertThat(records.getAllValues()).extracting(InventoryRecord::getKind)
                .containsExactly(InventoryRecord.Kind.AUDIT, InventoryRecord.Kind.LOW_STOCK, InventoryRecord.Kind.AUDIT, InventoryRecord.Kind.AUDIT);
        var alert = records.getAllValues().get(1);
        assertThat(alert.getProductName()).isEqualTo("Toy");
        assertThat(alert.getPreviousStock()).isEqualTo(4);
        assertThat(alert.getNewStock()).isEqualTo(3);
    }

    private Product product() {
        return new Product("Toy", "Cotton toy", BigDecimal.TEN, 4, "Toys", "/toy.svg");
    }
}
