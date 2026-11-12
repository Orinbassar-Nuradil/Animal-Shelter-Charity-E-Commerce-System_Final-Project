package com.pawhaven.patterns.observer;

import com.pawhaven.commerce.Product;

public record StockChange(Product product, int previousStock, int newStock, String reason) { }
