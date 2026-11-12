package com.pawhaven.patterns.builder;

import com.pawhaven.commerce.CommerceException;
import com.pawhaven.commerce.Money;
import java.math.BigDecimal;

/** A snapshot of a catalog line at checkout time. */
public record DraftItem(long productId, String productName, int quantity, BigDecimal unitPrice) {
    public DraftItem {
        if (productId < 1 || productName == null || productName.isBlank() || quantity < 1 || quantity > 999
                || unitPrice == null || unitPrice.signum() <= 0 || unitPrice.stripTrailingZeros().scale() > 2) {
            throw new CommerceException("Each order item needs a product, a valid quantity and a positive unit price.");
        }
        unitPrice = Money.round(unitPrice);
    }

    public BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
