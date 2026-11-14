package com.pawhaven.patterns.decorator;

import java.util.Objects;

/** Delegates to an inner component so checkout options can be combined at runtime. */
public abstract class AbstractCheckoutPriceDecorator implements CheckoutPriceComponent {
    protected final CheckoutPriceComponent inner;

    protected AbstractCheckoutPriceDecorator(CheckoutPriceComponent inner) {
        this.inner = Objects.requireNonNull(inner);
    }
}

// Base decorator wrapper
