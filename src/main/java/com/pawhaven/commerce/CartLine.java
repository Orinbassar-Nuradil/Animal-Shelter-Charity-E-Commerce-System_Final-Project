package com.pawhaven.commerce;

import java.math.BigDecimal;

public record CartLine(Product product, int quantity, BigDecimal subtotal) { }
