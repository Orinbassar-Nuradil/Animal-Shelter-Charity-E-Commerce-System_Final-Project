package com.pawhaven.commerce;

import com.pawhaven.patterns.builder.DraftItem;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private ShelterOrder order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItem() { }

    OrderItem(ShelterOrder order, Product product, DraftItem snapshot) {
        this.order = order;
        this.product = product;
        productName = snapshot.productName();
        quantity = snapshot.quantity();
        unitPrice = snapshot.unitPrice();
    }

    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
