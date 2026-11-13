# PawHaven — Animal Shelter & Charity E-Commerce System

PawHaven is a local university project for a three-person Software Design Patterns defense. Visitors apply to adopt cats and dogs, buy charity merchandise and make simulated donations. A shelter administrator reviews placements and manages inventory. Adoption requires staff approval and carries no purchase price.

## Run on Windows

Use a **JDK 17 or 21** with `JAVA_HOME` pointing to the JDK root. This workspace was built with Temurin 17. The included Maven Wrapper uses Maven 3.9.11. The first build needs internet access to download Maven and the dependencies. The running application uses local assets, an embedded web server and a file-backed database.

1. Open PowerShell in the directory containing `pom.xml`.
2. Set the JDK for this terminal. On the computer used for verification, the installed JDK is at the path below. On another computer, substitute its JDK directory.

   ```powershell
   $env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-17.0.17"
   & "$env:JAVA_HOME\bin\java.exe" -version
   ```

3. Start the application.

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open [PawHaven](http://127.0.0.1:8080/) when the terminal reports `Started PawHavenApplication`. Press **Ctrl+C** in that terminal to stop it. An existing Maven installation can run the same goals with `mvn`.

For an offline defense, build the executable JAR once while the dependencies are available. From the project root, use the second command on the demonstration computer with its JDK configured. Run only one instance against each database file.

```powershell
.\mvnw.cmd clean package
& "$env:JAVA_HOME\bin\java.exe" -jar .\target\pawhaven-1.0.0.jar
```

### Staff login

Open the **Staff dashboard** link in the footer or visit [the dashboard](http://127.0.0.1:8080/admin). The local demo username is **admin** and the demo password is **ShelterDemo17!**. To change them, set these variables in PowerShell before starting the app.

```powershell
$env:PAWHAVEN_ADMIN_USERNAME = 'shelter-staff'
$env:PAWHAVEN_ADMIN_PASSWORD = 'your-local-demo-password'
```

Spring Security protects the admin routes by role and checks CSRF tokens on writes. The app binds to the loopback interface by default. Use sample applicant and customer information during the defense.

## What to demonstrate

| Area | Entry point | Actions |
| --- | --- | --- |
| Home | `/` | See featured residents, merchandise, record-based statistics and the adoption process. |
| Adoption | `/animals` | Filter by species or status, open a profile, then apply with contact and care information. |
| Charity shop | `/shop` | Browse by category and add merchandise to a session cart. Prices are displayed in KZT (₸). |
| Cart and checkout | `/cart`, `/checkout` | Edit quantities, choose pickup or delivery, apply a promo, review extras and place a demo order. |
| Donations | `/support` | Select a suggested amount or enter a custom amount to record a simulated contribution. |
| Administration | `/admin` | Create animals, place them on hold, review applications, adjust stock and inspect orders and inventory events. |

An application begins as **PENDING**. Approval marks the animal **ADOPTED** and rejects competing pending applications for that animal in the same transaction. Staff can temporarily move a resident between **AVAILABLE** and **ON_HOLD**. A standalone donation has its own record. A checkout donation appears in the stored order breakdown.

### Short defense walkthrough

1. On a freshly seeded database, open Maple’s profile and submit an application using a sample email and phone number. Sign in to the dashboard. Open **Applications**, read the care plan and approve the placement. Reopen Maple’s profile to see the adopted status.
2. Open **Animals → Add** in the staff area. Create one cat and one dog with different care notes. Filter the public catalog by each species to see the new profiles.
3. In the charity shop, add a **Shelter Canvas Tote** and a **Braided Rope Toy**. In the cart change the tote quantity to **2**. The merchandise subtotal is **15,500 ₸**.
4. Choose pickup or supply a sample delivery address. Preview with no coupon, then use **PAWS10** for a **1,550 ₸** discount. Edit the details to add **500 ₸** gift wrapping and a **400 ₸** donation. The final total should be **14,850 ₸**. The alternate code **HELP1500** discounts up to **1,500 ₸** of merchandise.
5. Select **Place demo order** and inspect the receipt. Check **Orders** in the dashboard for the same items and total. The tote’s stock falls from **5 to 3**. Open **Inventory history** to see the stock audit and the low-stock alert. Restock the tote and refresh the history to see the new adjustment.
6. Open **Support our shelter** and record a standalone demo donation. The standalone donation count updates on the dashboard.

## Design patterns

All six implementations live under `src/main/java/com/pawhaven/patterns`. This is a proposed division for preparation and team coordination. Each student can use the corresponding unit tests and live actions to explain their two patterns.

| Student | Pattern | Principal classes and executed behavior |
| --- | --- | --- |
| 1 | [Factory Method](src/main/java/com/pawhaven/patterns/factory/) | `AnimalCreator.createProfile` delegates to the overridable `createAnimal`. `CatCreator` and `DogCreator` construct the corresponding JPA subtype during intake. |
| 1 | [Builder](src/main/java/com/pawhaven/patterns/builder/) | `OrderDraft.Builder` assembles customer, item snapshots, fulfillment and extras. `OrderService.draft` calls `build()` during checkout. |
| 2 | [Strategy](src/main/java/com/pawhaven/patterns/strategy/) | `PricingService` selects a `DiscountStrategy`. The implementations are `NoDiscountStrategy`, `PercentageDiscountStrategy` and `FixedAmountDiscountStrategy`. |
| 2 | [Decorator](src/main/java/com/pawhaven/patterns/decorator/) | `BaseCheckoutPrice` implements `CheckoutPriceComponent`. `GiftWrappingDecorator` and `ShelterDonationDecorator` wrap it through `AbstractCheckoutPriceDecorator`. |
| 3 | [Observer](src/main/java/com/pawhaven/patterns/observer/) | `StockEventPublisher` notifies registered `InventoryObserver` instances. `InventoryAuditObserver` records each change and `LowStockObserver` records a downward crossing of the threshold. |
| 3 | [Facade](src/main/java/com/pawhaven/patterns/facade/) | `CheckoutFacade.placeOrder` coordinates locked stock checks, draft creation, pricing, demo payment, order storage and observed inventory updates. The cart clears after commit. |

The [defense guide](docs/DESIGN_PATTERNS.md) explains the class relationships, design choices, tests and demonstration steps for each pattern.

## Stack and source layout

Java 17, Spring Boot 3.5.16, Maven, Spring MVC, Spring Data JPA with Hibernate, Jakarta Bean Validation, Spring Security, Thymeleaf and H2. The frontend uses HTML, CSS, vanilla JavaScript and local image and font files. Pricing uses `BigDecimal` with two decimal places. The tests use JUnit 5, Spring Boot Test, MockMvc, AssertJ and Mockito.

| Directory under `src/main` | Contents |
| --- | --- |
| `java/com/pawhaven/adoption` | Animal inheritance, applications, forms, seed data, repositories and adoption transactions |
| `java/com/pawhaven/commerce` | Cart, pricing, order snapshots, inventory and product seeds |
| `java/com/pawhaven/patterns` | Six pattern packages shared by the workflows |
| `java/com/pawhaven/support` | Standalone donation form and persistence |
| `java/com/pawhaven/web`, `java/com/pawhaven/config` | Public and staff entry points, checkout controllers and security |
| `resources/templates`, `resources/static` | Server-rendered pages, layout fragments, stylesheets, JavaScript and offline assets |

## Persistence and reset

The default JDBC URL is `jdbc:h2:file:./data/pawhaven`, relative to the working directory. On first startup, the seeders populate empty animal and product tables with **12 animals** and **10 products**. Two residents already have homes and two are on hold. Animal and product seeding run independently inside transactions. Subsequent starts reuse the same data.

To reset the demo while retaining a backup, stop PawHaven, run the following from the project root, then start the application again.

```powershell
$backupName = 'data-backup-' + (Get-Date -Format 'yyyyMMdd-HHmmss')
Rename-Item -LiteralPath '.\data' -NewName $backupName
.\mvnw.cmd spring-boot:run
```

The previous database remains in the dated folder. Configuration lives in [application.properties](src/main/resources/application.properties). A different demo database can be selected with `SPRING_DATASOURCE_URL`.

## Run the tests

Run the suite from the project root using the same JDK configuration.

```powershell
.\mvnw.cmd clean test
```

Each Spring test context uses its own in-memory H2 database. The suite exercises all six patterns, competing adoption decisions, stale and repeated checkout requests, the last unit of stock, observer failure and rollback, server-side validation, role protection, CSRF and receipt access. Individual results are written to `target/surefire-reports`.

## Demo boundaries

- All payments, donations and fulfillment are simulated. There are no card fields, delivery integrations or email notifications.
- The cart and public receipt access belong to the current browser session. Orders, applicants, decisions, donations and stock history persist in H2. Staff can view saved orders after a restart.
- The in-memory staff account, file database and Hibernate schema updates suit a local, single-instance classroom demo. Staff orders and applications are shown as lists. Inventory history displays the latest 100 records on page load.
- The first three seeded residents have generated photo-style portraits. Other residents and products use local illustrations. New cat and dog profiles receive a species illustration. Local DM Sans and DM Serif Text font licenses are included with the assets.



