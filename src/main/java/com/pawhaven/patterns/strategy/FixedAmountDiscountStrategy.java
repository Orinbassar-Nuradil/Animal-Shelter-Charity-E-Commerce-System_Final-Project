package com.pawhaven.patterns.strategy;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {
    private static final BigDecimal AMOUNT = new BigDecimal("1500.00");

    public String code() { return "HELP1500"; }
    public BigDecimal discount(BigDecimal subtotal) { return subtotal.min(AMOUNT); }
}

// HELP1500 fixed discount strategy
