package com.pawhaven.commerce;

import com.pawhaven.patterns.builder.OrderDraft;
import com.pawhaven.patterns.builder.DraftItem;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shelter_orders")
public class ShelterOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private UUID receiptCode;
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String checkoutToken;
    @Column(nullable = false, length = 100)
    private String customerName;
    @Column(nullable = false, length = 254)
    private String email;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Fulfillment fulfillment;
    @Column(length = 500)
    private String address;
    @Column(nullable = false, length = 30)
    private String promoCode;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal subtotal;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal discount;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal giftWrappingFee;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal donation;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal total;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected ShelterOrder() { }

    ShelterOrder(OrderDraft draft, PriceBreakdown price, String token, UUID receiptCode) {
        this.receiptCode = receiptCode;
        checkoutToken = token;
        customerName = draft.customerName();
        email = draft.email();
        fulfillment = draft.fulfillment();
        address = draft.address();
        promoCode = draft.promoCode();
        subtotal = price.subtotal();
        discount = price.discount();
        giftWrappingFee = price.giftWrapping();
        donation = price.donation();
        total = price.total();
        createdAt = LocalDateTime.now();
        status = OrderStatus.DEMO_COMPLETED;
    }

    void addItem(Product product, DraftItem item) {
        items.add(new OrderItem(this, product, item));
    }

    public Long getId() { return id; }
    public UUID getReceiptCode() { return receiptCode; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public Fulfillment getFulfillment() { return fulfillment; }
    public String getAddress() { return address; }
    public String getPromoCode() { return promoCode; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getGiftWrappingFee() { return giftWrappingFee; }
    public BigDecimal getDonation() { return donation; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
}
