package com.pawhaven.commerce;

import com.pawhaven.patterns.builder.*;
import com.pawhaven.patterns.facade.CheckoutFacade;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutFacadeTest {
    @Test
    void cartIsClearedOnlyAfterTheTransactionManagerCommits() {
        var cart = new SessionCart();
        cart.put(1, 2);
        var request = new CheckoutRequest();
        request.setToken(cart.getToken());
        var carts = mock(CartService.class);
        var pricing = mock(PricingService.class);
        var orders = mock(OrderService.class);
        var inventory = mock(InventoryService.class);
        var payments = mock(DemoPaymentService.class);
        var manager = mock(PlatformTransactionManager.class);

        var current = new CartView(List.of(), new BigDecimal("200"), 2);
        var draft = OrderDraft.builder().customer("Alex", "alex@example.org")
                .fulfillment(Fulfillment.PICKUP).items(List.of(new DraftItem(1, "Toy", 2, new BigDecimal("100")))).build();
        var quote = new PriceBreakdown(current.subtotal(), Money.ZERO, Money.ZERO, Money.ZERO);
        var receipt = mock(ShelterOrder.class);
        UUID receiptCode = UUID.randomUUID();
        when(carts.lockForCheckout(cart)).thenReturn(current);
        when(orders.draft(request, current)).thenReturn(draft);
        when(pricing.quote(current.subtotal(), "", false, Money.ZERO)).thenReturn(quote);
        when(payments.simulate(quote.total())).thenReturn(receiptCode);
        when(orders.save(draft, quote, request.getToken(), receiptCode)).thenReturn(receipt);
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        doAnswer(call -> {
            assertThat(cart.getItemCount()).isEqualTo(2);
            return null;
        }).when(manager).commit(any());

        var facade = new CheckoutFacade(carts, pricing, orders, inventory, payments, manager);
        assertThat(facade.placeOrder(cart, request)).isSameAs(receipt);
        var flow = inOrder(payments, orders, inventory, manager);
        flow.verify(payments).simulate(quote.total());
        flow.verify(orders).save(draft, quote, request.getToken(), receiptCode);
        flow.verify(inventory).fulfill(current, receipt);
        flow.verify(manager).commit(any());
        assertThat(cart.getItemCount()).isZero();
    }
}
