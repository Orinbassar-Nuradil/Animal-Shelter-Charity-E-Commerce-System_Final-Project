package com.pawhaven.commerce;

import com.pawhaven.patterns.strategy.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DiscountStrategyTest {
    private final PricingService pricing = new PricingService(List.of(
            new NoDiscountStrategy(), new PercentageDiscountStrategy(), new FixedAmountDiscountStrategy()));

    @Test
    void selectingEachCodeChangesTheMerchandiseDiscount() {
        BigDecimal subtotal = new BigDecimal("4000.25");
        assertThat(pricing.quote(subtotal, "", false, null).discount()).isEqualByComparingTo("0.00");
        assertThat(pricing.quote(subtotal, " paws10 ", false, null).discount()).isEqualByComparingTo("400.03");
        assertThat(pricing.quote(subtotal, "HELP1500", false, null).discount()).isEqualByComparingTo("1500.00");
    }

    @Test
    void fixedDiscountIsCappedAndDoesNotConsumeOptionalExtras() {
        var price = pricing.quote(new BigDecimal("600"), "HELP1500", true, new BigDecimal("200"));
        assertThat(price.discount()).isEqualByComparingTo("600");
        assertThat(price.total()).isEqualByComparingTo("700");
    }

    @Test
    void invalidCodeIsRejected() {
        assertThatThrownBy(() -> pricing.quote(BigDecimal.TEN, "NOPE", false, null))
                .isInstanceOf(CommerceException.class).hasMessageContaining("code");
    }
}
