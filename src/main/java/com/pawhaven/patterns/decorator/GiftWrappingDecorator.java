package com.pawhaven.patterns.decorator;

import com.pawhaven.commerce.PriceBreakdown;
import java.math.BigDecimal;

public class GiftWrappingDecorator extends AbstractCheckoutPriceDecorator {
    public static final BigDecimal FEE = new BigDecimal("500.00");

    public GiftWrappingDecorator(CheckoutPriceComponent inner) { super(inner); }

    public PriceBreakdown price() {
        PriceBreakdown base = inner.price();
        return new PriceBreakdown(base.subtotal(), base.discount(), base.giftWrapping().add(FEE), base.donation());
    }
}
