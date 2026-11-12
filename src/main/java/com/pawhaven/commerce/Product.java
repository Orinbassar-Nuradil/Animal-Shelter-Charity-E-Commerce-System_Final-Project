package com.pawhaven.commerce;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, length = 800)
    private String description;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private int stock;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false)
    private String imagePath;
    @Column(nullable = false)
    private boolean active;

    protected Product() { }

    public Product(String name, String description, BigDecimal price, int stock, String category, String imagePath) {
        if (Objects.requireNonNull(price).signum() <= 0 || price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Products need a positive unit price in tenge.");
        }
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.price = Money.round(price);
        this.category = Objects.requireNonNull(category);
        this.imagePath = Objects.requireNonNull(imagePath);
        this.active = true;
        setStock(stock);
    }

    void setStock(int quantity) {
        if (quantity < 0 || quantity > 1_000_000) {
            throw new CommerceException("Stock must be between 0 and 1,000,000 units.");
        }
        this.stock = quantity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
    public boolean isActive() { return active; }
    public boolean isAvailable() { return active && stock > 0; }
}
