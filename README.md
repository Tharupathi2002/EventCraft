# Budget & Expense Management -- EventCraft

Spring Boot + Thymeleaf + JPA/SQL Server module that lets an event host set
a budget, record expenses against categories, and track a live dashboard
with remaining balance and an overspend warning.

Rebuilt to match the team's ER diagram: `Budget` (BudgetID, EventID,
TotalBudget, RemainingBalance) and `Expenses` (ExpenseID, Category,
BudgetId, Date, Amount).

## A few deliberate choices beyond the strict diagram

- **RemainingBalance is computed, not stored.** It's always
  `TotalBudget - sum(expenses)`, calculated on read -- so it can never
  drift out of sync with the expenses. See `BudgetSummary`.
- **EventName and CreatedDate** are extra fields on `Budget` beyond the
  diagram, kept purely for usability (a human-readable label and a sort
  order). `EventId` itself is a plain required field, not a real foreign
  key -- the `Event` table lives in a different module/database, so this
  module stays fully standalone.
- **Description** is kept on `Expense` alongside `Category`, even though
  the diagram only lists `Category`. Without it, two expenses in the same
  category would be indistinguishable in the list. Remove it if you need
  an exact 1:1 match.
- **Category** is a plain string field (per the diagram), not a separate
  managed entity/table. The fixed list of options shown in the UI dropdown
  lives in `Expense.CATEGORIES` (Venue, Catering, Decorations, Photography,
  Entertainment, Transportation, Other) -- edit that list to change the
  choices.
- **Sandbox "Pay" feature** on expenses (paid/paymentReference/paidDate/
  cardLast4) is a bonus layered on top -- the diagram's own `Payment`
  entity is attached to `Booking`, not `Expenses`. It's a fully
  self-contained fake gateway (no network calls, no real processor); a
  card ending in `0002` always declines, for testing that path.

## Package layout

```
se.it25102007.budgetmanagement
├── BudgetManagementApplication.java
├── model/          Budget, Expense (JPA entities)
├── repository/     Spring Data JPA repositories
├── service/        BudgetService, ExpenseService, PaymentSandboxService
├── controller/      HomeController, BudgetController, ExpenseController
├── dto/             BudgetSummary, CategoryTotal, PaymentSummary,
│                     PaymentRequest, PaymentResult
└── exception/       ResourceNotFoundException
```

## Runs completely standalone

- **Own build**: its own `pom.xml`, buildable with `mvn spring-boot:run`
- **Own UI**: all pages are this app's own Thymeleaf templates
- **Own database**: `eventcraft_budget_db`, with its own `budget` and
  `expense` tables that no other module reads or writes
- **Own port**: `8082` by default, so it won't collide with a teammate's
  module running on `8080`

## Running it

Requires JDK 17+, Maven, and a running SQL Server instance (managed
through SSMS).

### 1. Create the database in SSMS

```sql
CREATE DATABASE eventcraft_budget_db;
```

### 2. Point the app at your instance

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=eventcraft_budget_db;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong!Passw0rd
```

- Default instance -> `localhost:1433`
- Named instance (e.g. SQLEXPRESS) -> `localhost\SQLEXPRESS` (drop the
  port), and make sure the "SQL Server Browser" Windows service is running
- SQL Server Authentication (mixed mode) must be enabled in SSMS

### 3. Run it

```bash
mvn spring-boot:run
```

Open **http://localhost:8082** -- it redirects to `/budgets`, which starts
empty (no seed/demo data). Hibernate creates the `budget` and `expense`
tables automatically (`spring.jpa.hibernate.ddl-auto=create-drop`), so each
restart gives you a fresh schema. Switch that setting to `update` once you
want data to survive restarts.

### Optional: run against H2 instead

Comment out the SQL Server block in `application.properties` and uncomment
the H2 block underneath it -- no SQL Server needed at all.

## Features

- **Create**: new budget (event id, event name, total budget), new
  expenses (description, category, amount, date)
- **Read**: budget list, dashboard with an Expenses tab (search + filter
  by category) and a Summary tab (paid vs. unpaid totals, a Pending
  Payments list with one-click Pay buttons, and spending-by-category
  breakdown with percent of total budget)
- **Update**: edit budget, edit expense
- **Delete**: delete budget (cascades its expenses) or a single expense,
  both through a single reusable in-page confirmation dialog rather than
  the browser's native `confirm()` (which is unreliable inside embedded/
  IDE browser previews)
- The UI is intentionally plain (no custom theming, no emoji/imagery) so
  it's easy to restyle once this module is wired into the rest of
  EventCraft
