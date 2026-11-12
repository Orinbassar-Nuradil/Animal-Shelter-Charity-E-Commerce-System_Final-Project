package com.pawhaven.commerce;

import com.pawhaven.patterns.decorator.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CheckoutDecoratorTest {
    private final CheckoutPriceComponent base = new BaseCheckoutPrice(new BigDecimal("5000"), new BigDecimal("500"));

    @Test
    void eachLayerPreservesTheBaseAndAddsOnlyItsOwnFee() {
        assertThat(base.price().total()).isEqualByComparingTo("4500");
        assertThat(new GiftWrappingDecorator(base).price().total()).isEqualByComparingTo("5000");
        assertThat(new ShelterDonationDecorator(base, new BigDecimal("200")).price().total()).isEqualByComparingTo("4700");
        var both = new ShelterDonationDecorator(new GiftWrappingDecorator(base), new BigDecimal("200")).price();
        assertThat(both.total()).isEqualByComparingTo("5200");
        assertThat(both.giftWrapping()).isEqualByComparingTo("500");
        assertThat(both.donation()).isEqualByComparingTo("200");
        assertThat(base.price().giftWrapping()).isEqualByComparingTo("0");
    }

    @Test
    void negativeAndSubcentDonationsAreRejected() {
        assertThatThrownBy(() -> new ShelterDonationDecorator(base, new BigDecimal("-0.01"))).isInstanceOf(CommerceException.class);
        assertThatThrownBy(() -> new ShelterDonationDecorator(base, new BigDecimal("0.001"))).isInstanceOf(CommerceException.class);
    }
}
