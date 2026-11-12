package com.pawhaven.commerce;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CheckoutRequest implements java.io.Serializable {
    @NotBlank @Size(max = 100)
    private String customerName;
    @NotBlank @Email @Size(max = 254)
    private String email;
    @NotNull
    private Fulfillment fulfillment = Fulfillment.PICKUP;
    @Size(max = 500)
    private String address;
    @Size(max = 30)
    private String promoCode;
    private boolean giftWrapping;
    @DecimalMin("0.00") @DecimalMax("1000000.00") @Digits(integer = 7, fraction = 2)
    private BigDecimal donation = Money.ZERO;
    @NotBlank @Size(max = 36)
    private String token;

    @AssertTrue(message = "Enter a delivery address or select shelter pickup.")
    public boolean isDeliveryAddressValid() {
        return fulfillment != Fulfillment.DELIVERY || (address != null && !address.isBlank());
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String value) { customerName = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public Fulfillment getFulfillment() { return fulfillment; }
    public void setFulfillment(Fulfillment value) { fulfillment = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String value) { promoCode = value; }
    public boolean isGiftWrapping() { return giftWrapping; }
    public void setGiftWrapping(boolean value) { giftWrapping = value; }
    public BigDecimal getDonation() { return donation; }
    public void setDonation(BigDecimal value) { donation = value; }
    public String getToken() { return token; }
    public void setToken(String value) { token = value; }
}
