package com.pawhaven.patterns.strategy;

import com.pawhaven.commerce.Money;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NoDiscountStrategy implements DiscountStrategy {
    public String code() { return ""; }
    public BigDecimal discount(BigDecimal subtotal) { return Money.ZERO; }
}
