package com.pawhaven.patterns.builder;

import com.pawhaven.commerce.CommerceException;
import com.pawhaven.commerce.Fulfillment;
import com.pawhaven.commerce.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/** Collects required details and optional extras, then freezes a validated order snapshot. */
public final class OrderDraft {
    private final String customerName;
    private final String email;
    private final List<DraftItem> items;
    private final Fulfillment fulfillment;
    private final String address;
    private final String promoCode;
    private final boolean giftWrapping;
    private final BigDecimal donation;

    private OrderDraft(Builder builder) {
        customerName = required(builder.customerName, 100, "Enter a customer name.");
        email = required(builder.email, 254, "Enter a customer email address.");
        if (!email.matches("[^\\s@]+@[^\\s@]+")) throw new CommerceException("Enter a valid email address.");
        if (builder.items == null || builder.items.isEmpty()) throw new CommerceException("Your cart is empty.");
        if (builder.items.stream().anyMatch(java.util.Objects::isNull)) throw new CommerceException("An item is missing.");
        items = List.copyOf(builder.items);
        if (builder.fulfillment == null) throw new CommerceException("Select pickup or delivery.");
        fulfillment = builder.fulfillment;
        address = fulfillment == Fulfillment.DELIVERY
                ? required(builder.address, 500, "Enter a delivery address of up to 500 characters.") : null;
        promoCode = builder.promoCode == null ? "" : builder.promoCode.strip().toUpperCase(Locale.ROOT);
        if (promoCode.length() > 30) throw new CommerceException("The promotional code is too long.");
        giftWrapping = builder.giftWrapping;
        donation = Money.donation(builder.donation);
    }

    private static String required(String value, int maxLength, String message) {
        if (value == null || value.isBlank() || value.strip().length() > maxLength) throw new CommerceException(message);
        return value.strip();
    }

    public static Builder builder() { return new Builder(); }
    public String customerName() { return customerName; }
    public String email() { return email; }
    public List<DraftItem> items() { return items; }
    public Fulfillment fulfillment() { return fulfillment; }
    public String address() { return address; }
    public String promoCode() { return promoCode; }
    public boolean giftWrapping() { return giftWrapping; }
    public BigDecimal donation() { return donation; }

    public static final class Builder {
        private String customerName;
        private String email;
        private List<DraftItem> items;
        private Fulfillment fulfillment;
        private String address;
        private String promoCode;
        private boolean giftWrapping;
        private BigDecimal donation;

        private Builder() { }

        public Builder customer(String name, String email) {
            this.customerName = name;
            this.email = email;
            return this;
        }

        public Builder items(List<DraftItem> items) { this.items = items; return this; }
        public Builder fulfillment(Fulfillment fulfillment) { this.fulfillment = fulfillment; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder promoCode(String promoCode) { this.promoCode = promoCode; return this; }
        public Builder giftWrapping(boolean enabled) { this.giftWrapping = enabled; return this; }
        public Builder donation(BigDecimal amount) { this.donation = amount; return this; }
        public OrderDraft build() { return new OrderDraft(this); }
    }
}
