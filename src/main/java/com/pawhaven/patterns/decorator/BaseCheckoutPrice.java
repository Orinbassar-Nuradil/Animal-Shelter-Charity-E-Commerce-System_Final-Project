package com.pawhaven.patterns.decorator;

import com.pawhaven.commerce.Money;
import com.pawhaven.commerce.PriceBreakdown;
import java.math.BigDecimal;

public record BaseCheckoutPrice(BigDecimal subtotal, BigDecimal discount) implements CheckoutPriceComponent {
    public BaseCheckoutPrice {
        if (subtotal.signum() < 0 || discount.signum() < 0 || discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Discount must be between zero and the merchandise subtotal.");
        }
    }

    public PriceBreakdown price() {
        return new PriceBreakdown(subtotal, discount, Money.ZERO, Money.ZERO);
    }
}
