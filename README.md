### Car Repair Service Management System

This is a Java project for managing car repair orders in an auto service. It contains the main business logic for cars, clients, mechanics, repair work, and repair orders.
The project is currently focused on the domain model, business rules, and unit testing. It does not have a finished user interface or a database.

### Features
- Store basic information about clients (including personal discount rate) and their cars.
- Create repair orders and track their lifecycle: CREATED - DIAGNOSED - APPROVED - IN_PROGRESS - COMPLETED (or CANCELLED).
- Add repair work items and track labor costs together with spare parts costs.
- Assign an available mechanic with the required specialization when the order is approved.
- Mark repair work items as completed.
- Complete or cancel repair orders when the domain rules allow it.
- Prevent invalid state changes (for example, completing finished work or skipping stages).
- Calculate prices accurately: base price, client discount, and minimum order fee adjustment.
- Return a detailed price breakdown with line-by-line financial values.
- Save and search repair orders in a simple in-memory repository.

### Technologies
- Java 21 — the programming language.
- Maven — manages the project and its dependencies.
- JUnit 5 — used for regular, nested, and parameterized tests.
- Mockito — used for unit testing the service layer.
- Lombok — reduces boilerplate code.

### Main Classes
- Car — stores information about a car (VIN, make, model, year) and its owner.
- Client — stores client details (ID, name, phone, personal discount percentage).
- Mechanic — stores a mechanic's name, specialization, and availability status.
- WorkItem — describes repair work, labor cost, spare parts, and completion status.
- SparePart — stores part ID, name, brand, and unit price.
- RepairOrder — manages the repair process, status, assigned mechanic, and work items.
- StandardPricingPolicy — calculates the final price, applying client discounts and minimum order limits.
- PriceBreakdown — stores the complete financial breakdown (base price, discount, adjustments, final sum).
- InMemoryRepairOrderRepository — stores orders in memory while the program is running.
- RepairOrderService — coordinates repository operations, order state transitions, and checkout logic.

### Requirements
Before you start, install:
- Java Development Kit (JDK) 21 or later.
- Apache Maven.
- An IDE such as IntelliJ IDEA.

### How to Open the Project
1. Download or clone this project repository.
2. Open the project folder in IntelliJ IDEA.
3. Wait for Maven to load dependencies from pom.xml.
4. Make sure the project uses JDK 21.
5. Run the unit tests via terminal:

### Current Limitations
- Data is stored only in memory and resets when the program stops.
- There is no user interface, console menu, or database yet.
- The main method is only a quick demo, not a finished application.

### Future Improvements
- Connect a real database to save data permanently.
- Add a user-friendly console menu or web interface.