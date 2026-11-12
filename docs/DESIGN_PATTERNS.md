# PawHaven design patterns

Use this guide with a running local instance. The six patterns connect animal intake, checkout and inventory management. Animal adoption has its own application and review workflow. Shop orders contain merchandise.

## Proposed defense responsibilities

| Student role | Patterns to prepare | What to show |
| --- | --- | --- |
| Student 1 | Factory Method and Builder | Create both species, then assemble an order with a fulfillment choice. |
| Student 2 | Strategy and Decorator | Change a promotion and then layer optional charges onto the discounted price. |
| Student 3 | Observer and Facade | Complete the order and trace the resulting inventory records. |

The roles are a proposed coordination plan. The team can assign its own presenters.

## 1. Factory Method — shelter intake

**Problem.** Staff use one intake form for cats and dogs. Both species share profile fields, while each new record must have the correct concrete Java and JPA type.

**Classes and trigger.** `AdminAdoptionController` accepts the form at `POST /admin/animals`. `AdoptionService.addAnimal` converts it to an `AnimalProfile`. The service selects an `AnimalCreator` from an enum-keyed map built from the injected creator beans. The shared `createProfile` operation calls the protected, abstract factory method `createAnimal`. `CatCreator` instantiates `Cat` and `DogCreator` instantiates `Dog`. Both extend `Animal`, and a JPA discriminator preserves their types in the animals table. [Creators](../src/main/java/com/pawhaven/patterns/factory/) · [Adoption service](../src/main/java/com/pawhaven/adoption/AdoptionService.java)

**Design choice.** The service owns persistence and validation. Each creator owns knowledge of one concrete animal constructor. Changes to species-specific construction stay in that creator, and all callers use the same intake operation.

**Demonstrate it.** Sign in at `/admin`, open `/admin/animals/new`, enter a cat profile and save. Repeat for a dog. Open `/animals` and filter by each species to show the two new profiles. In a debugger, stop at `AnimalCreator.createProfile` and step into the overridden method for each submission. [Factory tests](../src/test/java/com/pawhaven/patterns/factory/AnimalCreatorTest.java)

## 2. Builder — a validated order draft

**Problem.** Every checkout has a customer, cart lines and a fulfillment preference. Only delivery needs an address. Promotions, wrapping and donations are optional.

**Classes and trigger.** When a visitor confirms a reviewed order at `POST /checkout`, `CheckoutFacade` obtains fresh, locked product rows and calls `OrderService.draft`. That method creates immutable `DraftItem` snapshots of the product ID, name, unit price and quantity. It passes those snapshots and the reviewed choices through the named steps of `OrderDraft.Builder`. The final `build()` checks required fields, item presence, fulfillment and the donation amount. A pickup draft omits the address. The item list is copied when the builder completes. [Builder](../src/main/java/com/pawhaven/patterns/builder/OrderDraft.java) · [Checkout assembly](../src/main/java/com/pawhaven/commerce/OrderService.java)

**Design choice.** Named steps make a call with customer details and several optional fields readable. Validation at construction keeps incomplete drafts out of persistence. Snapshotting the unit price and product name also preserves an order's history after catalog changes.

**Demonstrate it.** Put merchandise in the cart and open `/checkout`. Start with pickup. Edit the details on the review page and choose delivery with a sample address. Place the order and inspect the delivery information and items on the receipt. Step through `OrderService.draft` to see what each builder step contributes. [Builder tests](../src/test/java/com/pawhaven/commerce/OrderDraftTest.java)

## 3. Strategy — promotional discounts

**Problem.** The same subtotal must support different discount rules. A fixed discount may exceed the price of a small order.

**Classes and trigger.** The review and placement operations both call `PricingService.quote`. The service normalizes the supplied code, looks up a `DiscountStrategy` and calls `discount(subtotal)`. The original cart prices always come from the database. An unrecognized code produces a form error. [Strategies](../src/main/java/com/pawhaven/patterns/strategy/) · [Pricing service](../src/main/java/com/pawhaven/commerce/PricingService.java)

| Code | Implementation | Calculation |
| --- | --- | --- |
| Empty | `NoDiscountStrategy` | Zero discount |
| `PAWS10` | `PercentageDiscountStrategy` | 10% of merchandise, rounded half up to two decimal places |
| `HELP1500` | `FixedAmountDiscountStrategy` | The smaller of 1,500 ₸ and the merchandise subtotal |

**Design choice.** Each algorithm can be exercised independently. Spring supplies the implementations to a common lookup in the pricing service, so checkout and review share identical promotion rules.

**Demonstrate it.** Add two Shelter Canvas Totes and one Catnip Mouse. The fresh seed subtotal is 14,200 ₸. Use pickup and leave extras empty. Review once with no code, then edit and review with each promotion. The totals should be 14,200 ₸, 12,780 ₸ with `PAWS10`, and 12,700 ₸ with `HELP1500`. To see the fixed cap, preview just one 1,200 ₸ Catnip Mouse with `HELP1500`. [Strategy tests](../src/test/java/com/pawhaven/commerce/DiscountStrategyTest.java)

## 4. Decorator — optional checkout extras

**Problem.** A visitor can choose wrapping, a contribution, both or neither. The merchandise discount applies before these extras.

**Classes and trigger.** Within `PricingService.quote`, `BaseCheckoutPrice` starts a `CheckoutPriceComponent` with the subtotal and selected discount. `GiftWrappingDecorator` and `ShelterDonationDecorator` extend `AbstractCheckoutPriceDecorator`. Each holds another component, asks it for its breakdown, and returns a new breakdown with its own charge added. The same operation renders `/checkout/review` and prices the order on final confirmation. [Components and decorators](../src/main/java/com/pawhaven/patterns/decorator/) · [Price breakdown](../src/main/java/com/pawhaven/commerce/PriceBreakdown.java)

**Design choice.** Each extra has a single calculation. Composing the selected wrappers avoids maintaining a distinct pricing class or calculation for every combination. The breakdown retains separate fields for merchandise, discount, wrapping and donation.

**Demonstrate it.** On the 14,200 ₸ cart, select `PAWS10` to get 12,780 ₸. Edit checkout to add wrapping. The total becomes 13,280 ₸. Add a 300 ₸ donation and review again to see 13,580 ₸. The promotion remains 1,420 ₸ in all three cases. [Decorator tests](../src/test/java/com/pawhaven/commerce/CheckoutDecoratorTest.java)

## 5. Observer — inventory audit and low-stock alerts

**Problem.** Staff need a history of stock movements and a separate signal when an item first enters the low-stock range. Both purchases and manual stock adjustments cause changes.

**Classes and trigger.** `InventoryService.fulfill` reduces stock during checkout. `InventoryService.setStock` handles `POST /admin/products/{id}/stock`. Both send a `StockChange` to `StockEventPublisher`. The publisher registers injected `InventoryObserver` implementations, then calls each subscriber synchronously on an actual change. `InventoryAuditObserver` saves an audit row with the old and new levels and the reason. `LowStockObserver` writes an additional alert when stock falls from above 3 to 3 or below. Remaining inside the low range produces no repeated alert. [Observer types](../src/main/java/com/pawhaven/patterns/observer/) · [Stock updates](../src/main/java/com/pawhaven/commerce/InventoryService.java)

**Design choice.** The stock update code reports one event. Each subscriber implements its own reaction. Synchronous delivery lets the stock update, order and both observer outputs commit together. A subscriber failure rolls back the enclosing transaction.

**Demonstrate it.** Check the Tote in `/admin/products`. On a fresh database it starts at 5. Complete a checkout for two totes and open `/admin/inventory`. Find the audit row showing 5 → 3 and the low-stock alert for the same order. Change stock to 8 in the products screen and reopen history to find the 3 → 8 admin adjustment. The inventory page loads the 100 latest records on each request. [Observer tests](../src/test/java/com/pawhaven/commerce/StockObserverTest.java)

## 6. Facade — completing checkout

**Problem.** One confirmation has to recheck the cart and stock, price current merchandise, simulate payment, persist order lines and update inventory together. A second browser tab might reuse an old review, and two customers might try to buy the same final unit.

**Classes and trigger.** The **Place demo order** button sends the review ID to `POST /checkout`. `CheckoutController` retrieves the reviewed choices from the HTTP session and calls `CheckoutFacade.placeOrder(cart, request)`. [Facade](../src/main/java/com/pawhaven/patterns/facade/CheckoutFacade.java) · [Checkout controller](../src/main/java/com/pawhaven/web/commerce/CheckoutController.java)

1. The facade holds the session cart lock and checks its checkout token. Editing a cart rotates the token. Replaying an already completed review in the same session redirects to its existing receipt.
2. A transaction starts. `CartService.lockForCheckout` reads the product rows in ascending ID order with pessimistic write locks, then validates quantities against current stock.
3. `OrderService` builds the order draft. `PricingService` applies the Strategy and Decorator implementations to current prices.
4. `DemoPaymentService` issues a local reference for the simulated payment. `OrderService` saves the order and its item snapshots. `InventoryService` updates quantities and publishes the changes to its observers.
5. After the transaction commits, the facade clears the cart and returns the order. A failed stock check or observer write leaves the cart available for correction or retry.

**Design choice.** The controller deals with HTTP, review state and receipt access. The facade owns the sequence and transaction boundary. The underlying services keep their domain rules available for direct tests and for other relevant flows such as admin restocking.

**Demonstrate it.** Confirm the cart from the Decorator example and record the order number and final total on the receipt. Open `/admin/orders` to see the same stored items and amount, then `/admin/inventory` for the stock changes. In a second session, review an order and reduce a product's stock below the requested quantity using the admin account before confirming. The checkout reports the stock shortage and preserves the cart. [Facade unit test](../src/test/java/com/pawhaven/commerce/CheckoutFacadeTest.java) · [Transaction and concurrency tests](../src/test/java/com/pawhaven/commerce/CheckoutIntegrationTest.java)
