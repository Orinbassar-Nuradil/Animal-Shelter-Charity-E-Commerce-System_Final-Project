package com.pawhaven.support;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class DonationForm {
    @NotNull(message = "Enter a donation amount.")
    @DecimalMin(value = "1.00", message = "Choose at least 1 ₸.")
    @DecimalMax(value = "1000000.00", message = "Choose an amount up to 1,000,000 ₸.")
    @Digits(integer = 7, fraction = 2, message = "Use up to two decimal places.")
    private BigDecimal amount;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
