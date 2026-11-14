package com.pawhaven.patterns.decorator;

import com.pawhaven.commerce.Money;
import com.pawhaven.commerce.PriceBreakdown;
import java.math.BigDecimal;

public class ShelterDonationDecorator extends AbstractCheckoutPriceDecorator {
    private final BigDecimal amount;

    public ShelterDonationDecorator(CheckoutPriceComponent inner, BigDecimal amount) {
        super(inner);
        this.amount = Money.donation(amount);
    }

    public PriceBreakdown price() {
        PriceBreakdown base = inner.price();
        return new PriceBreakdown(base.subtotal(), base.discount(), base.giftWrapping(), base.donation().add(amount));
    }
}

// Shelter donation decorator
