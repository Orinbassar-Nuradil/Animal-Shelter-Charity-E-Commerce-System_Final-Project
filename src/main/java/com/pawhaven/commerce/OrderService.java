package com.pawhaven.commerce;

import com.pawhaven.patterns.builder.DraftItem;
import com.pawhaven.patterns.builder.OrderDraft;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final ShelterOrderRepository orders;
    private final ProductRepository products;

    public OrderService(ShelterOrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    public OrderDraft draft(CheckoutRequest request, CartView cart) {
        List<DraftItem> items = cart.lines().stream().map(line ->
                new DraftItem(line.product().getId(), line.product().getName(), line.quantity(), line.product().getPrice())
        ).toList();
        return OrderDraft.builder()
                .customer(request.getCustomerName(), request.getEmail())
                .items(items)
                .fulfillment(request.getFulfillment())
                .address(request.getAddress())
                .promoCode(request.getPromoCode())
                .giftWrapping(request.isGiftWrapping())
                .donation(request.getDonation())
                .build();
    }

    public ShelterOrder save(OrderDraft draft, PriceBreakdown price, String token, UUID receiptCode) {
        ShelterOrder order = new ShelterOrder(draft, price, token, receiptCode);
        for (DraftItem item : draft.items()) {
            order.addItem(products.getReferenceById(item.productId()), item);
        }
        return orders.save(order);
    }
}
