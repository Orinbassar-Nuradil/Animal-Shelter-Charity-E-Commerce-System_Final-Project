package com.pawhaven.commerce;

import com.pawhaven.patterns.facade.CheckoutFacade;
import com.pawhaven.patterns.observer.InventoryObserver;
import com.pawhaven.patterns.observer.StockEventPublisher;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "pawhaven.test.suite=checkout")
class CheckoutIntegrationTest {
    @Autowired ProductRepository products;
    @Autowired ShelterOrderRepository orders;
    @Autowired InventoryRecordRepository history;
    @Autowired CartService carts;
    @Autowired CheckoutFacade checkout;
    @Autowired InventoryService inventory;
    @Autowired StockEventPublisher publisher;

    @Test
    void checkoutPersistsSnapshotsBreakdownAndObserversAndRejectsReplay() {
        Product product = product(5);
        SessionCart cart = new SessionCart();
        carts.add(cart, product.getId(), 2);
        CheckoutRequest request = request(cart);
        request.setPromoCode("PAWS10");
        request.setGiftWrapping(true);
        request.setDonation(new BigDecimal("250"));
        request.setFulfillment(Fulfillment.DELIVERY);
        request.setAddress("1 Demo Street");
        var preview = checkout.preview(cart, request);
        var receipt = checkout.placeOrder(cart, request);
        var stored = orders.findByReceiptCode(receipt.getReceiptCode()).orElseThrow();

        assertThat(stored.getStatus()).isEqualTo(OrderStatus.DEMO_COMPLETED);
        assertThat(stored.getAddress()).isEqualTo("1 Demo Street");
        assertThat(stored.getSubtotal()).isEqualByComparingTo("4000");
        assertThat(stored.getDiscount()).isEqualByComparingTo("400");
        assertThat(stored.getGiftWrappingFee()).isEqualByComparingTo("500");
        assertThat(stored.getDonation()).isEqualByComparingTo("250");
        assertThat(stored.getTotal()).isEqualByComparingTo("4350");
        assertThat(stored.getTotal()).isEqualByComparingTo(preview.total());
        assertThat(stored.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getUnitPrice()).isEqualByComparingTo("2000");
            assertThat(item.getProductName()).isEqualTo(product.getName());
        });
        assertThat(stock(product)).isEqualTo(3);
        assertThat(events(product)).extracting(InventoryRecord::getKind)
                .containsExactlyInAnyOrder(InventoryRecord.Kind.AUDIT, InventoryRecord.Kind.LOW_STOCK);
        assertThat(cart.getItemCount()).isZero();
        long orderCount = orders.count();
        assertThatThrownBy(() -> checkout.placeOrder(cart, request)).isInstanceOf(CommerceException.class);
        assertThat(orders.count()).isEqualTo(orderCount);
        assertThat(stock(product)).isEqualTo(3);
    }

    @Test
    void stockIsRecheckedAfterAnAdminAdjustmentAndOtherItemsAreUntouched() {
        Product first = product(6);
        Product soldOut = product(2);
        SessionCart cart = new SessionCart();
        carts.add(cart, first.getId(), 1);
        carts.add(cart, soldOut.getId(), 2);
        inventory.setStock(soldOut.getId(), 0);
        assertThat(events(soldOut)).singleElement().satisfies(event -> {
            assertThat(event.getReason()).isEqualTo("Admin stock adjustment");
            assertThat(event.getKind()).isEqualTo(InventoryRecord.Kind.AUDIT);
        });
        long before = orders.count();
        assertThatThrownBy(() -> checkout.placeOrder(cart, request(cart))).hasMessageContaining("stock");
        assertThat(orders.count()).isEqualTo(before);
        assertThat(stock(first)).isEqualTo(6);
        assertThat(events(first)).isEmpty();
        assertThat(cart.getItemCount()).isEqualTo(3);
    }

    @Test
    void observerFailureRollsBackOrderStockAndEarlierAuditInserts() {
        Product product = product(4);
        SessionCart cart = new SessionCart();
        carts.add(cart, product.getId(), 1);
        InventoryObserver failing = event -> { throw new IllegalStateException("Observer unavailable"); };
        long before = orders.count();
        publisher.register(failing);
        try {
            assertThatThrownBy(() -> checkout.placeOrder(cart, request(cart))).hasMessage("Observer unavailable");
        } finally {
            publisher.unregister(failing);
        }
        assertThat(orders.count()).isEqualTo(before);
        assertThat(stock(product)).isEqualTo(4);
        assertThat(events(product)).isEmpty();
        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(checkout.placeOrder(cart, request(cart)).getTotal()).isEqualByComparingTo("2000");
    }

    @Test
    void twoCustomersCompetingForTheLastItemCannotOversellIt() throws Exception {
        Product product = product(1);
        var first = new SessionCart();
        var second = new SessionCart();
        carts.add(first, product.getId(), 1);
        carts.add(second, product.getId(), 1);
        var start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        long before = orders.count();
        try {
            Future<Boolean> a = pool.submit(() -> attempt(first, start));
            Future<Boolean> b = pool.submit(() -> attempt(second, start));
            start.countDown();
            assertThat(List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            pool.shutdownNow();
        }
        assertThat(orders.count()).isEqualTo(before + 1);
        assertThat(stock(product)).isZero();
        assertThat(first.getItemCount() + second.getItemCount()).isEqualTo(1);
    }

    @Test
    void twoTabsSubmittingTheSameCartCanCreateOnlyOneOrder() throws Exception {
        Product product = product(5);
        SessionCart cart = new SessionCart();
        carts.add(cart, product.getId(), 1);
        CheckoutRequest reviewed = request(cart);
        long before = orders.count();
        var start = new CountDownLatch(1);
        Callable<Boolean> confirm = () -> {
            start.await();
            try {
                checkout.placeOrder(cart, reviewed);
                return true;
            } catch (CommerceException rejected) {
                assertThat(rejected).hasMessageContaining("already completed");
                return false;
            }
        };
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(confirm);
            var second = pool.submit(confirm);
            start.countDown();
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            pool.shutdownNow();
        }
        assertThat(orders.count()).isEqualTo(before + 1);
        assertThat(stock(product)).isEqualTo(4);
        assertThat(events(product)).hasSize(1);
        assertThat(cart.getItemCount()).isZero();
    }

    @Test
    void quantityTamperingAndAStaleCheckoutAreRejected() {
        var product = product(5);
        var cart = new SessionCart();
        assertThatThrownBy(() -> carts.add(cart, product.getId(), -1)).isInstanceOf(CommerceException.class);
        carts.add(cart, product.getId(), 1);
        var oldRequest = request(cart);
        assertThatThrownBy(() -> carts.add(cart, product.getId(), Integer.MAX_VALUE)).isInstanceOf(CommerceException.class);
        assertThatThrownBy(() -> carts.update(cart, product.getId(), 0)).isInstanceOf(CommerceException.class);
        carts.update(cart, product.getId(), 3);
        assertThatThrownBy(() -> checkout.placeOrder(cart, oldRequest)).hasMessageContaining("changed");
        assertThat(stock(product)).isEqualTo(5);
        assertThat(cart.getItemCount()).isEqualTo(3);
    }

    private boolean attempt(SessionCart cart, CountDownLatch start) throws InterruptedException {
        start.await();
        try {
            checkout.placeOrder(cart, request(cart));
            return true;
        } catch (CommerceException ex) {
            assertThat(ex).hasMessageContaining("stock");
            return false;
        }
    }

    private Product product(int stock) {
        return products.save(new Product("Test toy " + UUID.randomUUID(), "Cotton toy", new BigDecimal("2000"), stock, "Toys", "/toy.svg"));
    }

    private int stock(Product product) { return products.findById(product.getId()).orElseThrow().getStock(); }

    private List<InventoryRecord> events(Product product) {
        return history.findTop100ByOrderByIdDesc().stream()
                .filter(event -> event.getProductName().equals(product.getName())).toList();
    }

    private CheckoutRequest request(SessionCart cart) {
        var request = new CheckoutRequest();
        request.setToken(cart.getToken());
        request.setCustomerName("Demo User");
        request.setEmail("demo@example.org");
        return request;
    }
}
