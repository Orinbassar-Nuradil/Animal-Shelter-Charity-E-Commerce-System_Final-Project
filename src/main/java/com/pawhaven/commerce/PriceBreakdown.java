package com.pawhaven.commerce;

import java.math.BigDecimal;

public record PriceBreakdown(BigDecimal subtotal, BigDecimal discount, BigDecimal giftWrapping, BigDecimal donation) {
    public BigDecimal total() {
        return subtotal.subtract(discount).add(giftWrapping).add(donation);
    }
}
