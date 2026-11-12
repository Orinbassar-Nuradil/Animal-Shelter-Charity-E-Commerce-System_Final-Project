package com.pawhaven.commerce;

import java.math.BigDecimal;
import java.util.List;

public record CartView(List<CartLine> lines, BigDecimal subtotal, int itemCount) {
    public CartView {
        lines = List.copyOf(lines);
    }
}
