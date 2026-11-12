package com.pawhaven.patterns.decorator;

import com.pawhaven.commerce.PriceBreakdown;

/** Both the base quote and every optional layer provide the same price breakdown interface. */
public interface CheckoutPriceComponent {
    PriceBreakdown price();
}
