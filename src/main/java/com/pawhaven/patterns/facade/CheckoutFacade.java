package com.pawhaven.patterns.facade;

import com.pawhaven.commerce.*;
import com.pawhaven.patterns.builder.OrderDraft;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** One checkout call coordinates pricing, simulated payment, order persistence and observed stock changes. */
@Service
public class CheckoutFacade {
    private final CartService carts;
    private final PricingService pricing;
    private final OrderService orders;
    private final InventoryService inventory;
    private final DemoPaymentService payments;
    private final TransactionTemplate transaction;

    public CheckoutFacade(CartService carts, PricingService pricing, OrderService orders, InventoryService inventory,
                          DemoPaymentService payments, PlatformTransactionManager manager) {
        this.carts = carts;
        this.pricing = pricing;
        this.orders = orders;
        this.inventory = inventory;
        this.payments = payments;
        transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public PriceBreakdown preview(SessionCart cart, CheckoutRequest request) {
        CartView current = carts.view(cart);
        if (current.lines().isEmpty()) throw new CommerceException("Your cart is empty.");
        return pricing.quote(current.subtotal(), request.getPromoCode(), request.isGiftWrapping(), request.getDonation());
    }

    public ShelterOrder placeOrder(SessionCart cart, CheckoutRequest request) {
        // Hold the cart lock through commit. A second tab cannot mutate the cart or reuse this token.
        synchronized (cart) {
            if (!cart.getToken().equals(request.getToken())) {
                throw new CommerceException("Your cart has changed or this checkout was already completed. Review your cart.");
            }
            ShelterOrder order = transaction.execute(status -> complete(cart, request));
            cart.clear();
            return order;
        }
    }

    private ShelterOrder complete(SessionCart cart, CheckoutRequest request) {
        CartView locked = carts.lockForCheckout(cart);
        OrderDraft draft = orders.draft(request, locked);
        PriceBreakdown quote = pricing.quote(locked.subtotal(), draft.promoCode(), draft.giftWrapping(), draft.donation());
        UUID receiptCode = payments.simulate(quote.total());
        ShelterOrder order = orders.save(draft, quote, cart.getToken(), receiptCode);
        inventory.fulfill(locked, order);
        return order;
    }
}
