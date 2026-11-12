package com.pawhaven.patterns.strategy;

import java.math.BigDecimal;

/** Computes the discount on merchandise before optional checkout extras. */
public interface DiscountStrategy {
    String code();
    BigDecimal discount(BigDecimal subtotal);
}
