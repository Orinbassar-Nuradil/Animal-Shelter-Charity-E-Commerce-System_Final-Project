package com.pawhaven.commerce;

import com.pawhaven.patterns.builder.DraftItem;
import com.pawhaven.patterns.builder.OrderDraft;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderDraftTest {
    private final DraftItem bowl = new DraftItem(1, "Bowl", 2, new BigDecimal("3500.00"));

    @Test
    void builderFreezesCustomerAndItemsAndKeepsAllSelectedOptions() {
        var items = new ArrayList<>(List.of(bowl));
        var draft = OrderDraft.builder().customer("  Alex  ", "alex@example.org")
                .items(items).fulfillment(Fulfillment.DELIVERY).address(" 1 Demo Way ")
                .promoCode(" paws10 ").giftWrapping(true).donation(new BigDecimal("250")).build();
        items.clear();
        assertThat(draft.customerName()).isEqualTo("Alex");
        assertThat(draft.email()).isEqualTo("alex@example.org");
        assertThat(draft.items()).containsExactly(bowl);
        assertThat(draft.items().get(0).subtotal()).isEqualByComparingTo("7000");
        assertThat(draft.fulfillment()).isEqualTo(Fulfillment.DELIVERY);
        assertThat(draft.address()).isEqualTo("1 Demo Way");
        assertThat(draft.promoCode()).isEqualTo("PAWS10");
        assertThat(draft.giftWrapping()).isTrue();
        assertThat(draft.donation()).isEqualByComparingTo("250.00");
    }

    @Test
    void pickupDraftOmitsAddressAndDefaultsToNoExtras() {
        var draft = OrderDraft.builder().customer("Alex", "alex@example.org").items(List.of(bowl))
                .fulfillment(Fulfillment.PICKUP).address("irrelevant").build();
        assertThat(draft.address()).isNull();
        assertThat(draft.donation()).isEqualByComparingTo("0.00");
        assertThat(draft.giftWrapping()).isFalse();
    }

    @Test
    void builderRejectsIncompleteOrdersAndNegativeDonations() {
        assertThatThrownBy(() -> OrderDraft.builder().build()).isInstanceOf(CommerceException.class);
        var builder = OrderDraft.builder().customer("Alex", "alex@example.org").items(List.of(bowl));
        assertThatThrownBy(builder::build).hasMessageContaining("pickup or delivery");
        assertThatThrownBy(() -> builder.fulfillment(Fulfillment.DELIVERY).build()).hasMessageContaining("delivery address");
        assertThatThrownBy(() -> builder.fulfillment(Fulfillment.PICKUP).donation(new BigDecimal("-1")).build())
                .isInstanceOf(CommerceException.class);
        assertThatThrownBy(() -> builder.items(List.of()).build()).hasMessageContaining("empty");
        assertThatThrownBy(() -> new DraftItem(1, "Bowl", 0, BigDecimal.ONE)).isInstanceOf(CommerceException.class);
    }


    @Test
    void builderRejectsInvalidEmailAndLongPromoCode() {
        assertThatThrownBy(() -> OrderDraft.builder()
                .customer("Alex", "invalid-email")
                .items(List.of(bowl))
                .fulfillment(Fulfillment.PICKUP)
                .build())
                .isInstanceOf(CommerceException.class)
                .hasMessageContaining("valid email");

        assertThatThrownBy(() -> OrderDraft.builder()
                .customer("Alex", "alex@example.org")
                .items(List.of(bowl))
                .fulfillment(Fulfillment.PICKUP)
                .promoCode("A".repeat(31))
                .build())
                .isInstanceOf(CommerceException.class)
                .hasMessageContaining("too long");
    }

}

