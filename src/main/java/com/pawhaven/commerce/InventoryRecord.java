package com.pawhaven.commerce;

import com.pawhaven.patterns.observer.StockChange;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class InventoryRecord {
    public enum Kind { AUDIT, LOW_STOCK }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private int previousStock;
    @Column(nullable = false)
    private int newStock;
    @Column(nullable = false)
    private String reason;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Kind kind;

    protected InventoryRecord() { }

    public InventoryRecord(Kind kind, StockChange change) {
        this.kind = kind;
        product = change.product();
        productName = product.getName();
        previousStock = change.previousStock();
        newStock = change.newStock();
        reason = change.reason();
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public int getPreviousStock() { return previousStock; }
    public int getNewStock() { return newStock; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Kind getKind() { return kind; }
}
