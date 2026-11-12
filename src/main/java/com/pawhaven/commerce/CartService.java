package com.pawhaven.commerce;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    private final ProductRepository products;

    public CartService(ProductRepository products) {
        this.products = products;
    }

    public void add(SessionCart cart, long productId, int quantity) {
        synchronized (cart) {
            if (quantity < 1) throw new CommerceException("Choose at least one item.");
            long newQuantity = (long) cart.quantity(productId) + quantity;
            checkQuantity(product(productId), newQuantity);
            cart.put(productId, (int) newQuantity);
        }
    }

    public void update(SessionCart cart, long productId, int quantity) {
        synchronized (cart) {
            if (cart.quantity(productId) == 0) throw new CommerceException("This item is no longer in your cart.");
            checkQuantity(product(productId), quantity);
            cart.put(productId, quantity);
        }
    }

    public void remove(SessionCart cart, long productId) { cart.remove(productId); }

    public CartView view(SessionCart cart) { return read(cart, false); }

    /** Locks current product records in ID order inside the caller's checkout transaction. */
    public CartView lockForCheckout(SessionCart cart) { return read(cart, true); }

    private CartView read(SessionCart cart, boolean lock) {
        List<CartLine> lines = new ArrayList<>();
        BigDecimal subtotal = Money.ZERO;
        int itemCount = 0;
        for (Map.Entry<Long, Integer> item : cart.snapshot().entrySet()) {
            long id = item.getKey();
            Product product = lock
                    ? products.lockById(id).orElseThrow(() -> missingProduct())
                    : product(id);
            int quantity = item.getValue();
            if (lock) checkQuantity(product, quantity);
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            lines.add(new CartLine(product, quantity, lineTotal));
            subtotal = subtotal.add(lineTotal);
            itemCount += quantity;
        }
        return new CartView(lines, subtotal, itemCount);
    }

    private Product product(long id) { return products.findById(id).orElseThrow(this::missingProduct); }

    private CommerceException missingProduct() { return new CommerceException("This product could not be found."); }

    private void checkQuantity(Product product, long quantity) {
        if (quantity < 1 || quantity > 999) throw new CommerceException("Choose between 1 and 999 units per item.");
        if (!product.isActive() || quantity > product.getStock()) {
            throw new CommerceException("There is not enough stock of " + product.getName() + ". Please update your cart.");
        }
    }
}
