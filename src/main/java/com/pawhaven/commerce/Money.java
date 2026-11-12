package com.pawhaven.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    public static final BigDecimal MAX_DONATION = new BigDecimal("1000000.00");

    private Money() { }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal donation(BigDecimal amount) {
        if (amount == null) return ZERO;
        if (amount.signum() < 0 || amount.compareTo(MAX_DONATION) > 0
                || amount.stripTrailingZeros().scale() > 2) {
            throw new CommerceException("Enter a shelter donation from 0 to 1,000,000 ₸ with at most two decimal places.");
        }
        return round(amount);
    }
}
