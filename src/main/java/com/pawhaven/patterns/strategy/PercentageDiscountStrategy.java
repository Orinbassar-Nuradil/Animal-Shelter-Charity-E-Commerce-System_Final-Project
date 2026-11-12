package com.pawhaven.patterns.strategy;

import com.pawhaven.commerce.Money;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {
    private static final BigDecimal RATE = new BigDecimal("0.10");

    public String code() { return "PAWS10"; }
    public BigDecimal discount(BigDecimal subtotal) { return Money.round(subtotal.multiply(RATE)); }
}
