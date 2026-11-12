package com.pawhaven.commerce;

import com.pawhaven.patterns.observer.StockChange;
import com.pawhaven.patterns.observer.StockEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class InventoryService {
    private final ProductRepository products;
    private final StockEventPublisher publisher;

    public InventoryService(ProductRepository products, StockEventPublisher publisher) {
        this.products = products;
        this.publisher = publisher;
    }

    @Transactional
    public void setStock(long productId, int quantity) {
        Product product = products.lockById(productId).orElseThrow(() -> new CommerceException("This product could not be found."));
        change(product, quantity, "Admin stock adjustment");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void fulfill(CartView lockedCart, ShelterOrder order) {
        for (CartLine line : lockedCart.lines()) {
            Product product = line.product();
            change(product, product.getStock() - line.quantity(), "Demo order #" + order.getId());
        }
    }

    private void change(Product product, int quantity, String reason) {
        int before = product.getStock();
        if (before == quantity) return;
        product.setStock(quantity);
        publisher.publish(new StockChange(product, before, quantity, reason));
    }
}
