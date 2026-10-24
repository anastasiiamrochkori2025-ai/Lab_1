# Car Repair Service Management System

A Java-based domain model and business logic prototype for managing car repair orders in an auto service shop.

The project currently handles the core workflow in-memory: creating orders, tracking repair stages, assigning mechanics based on skills, and calculating total costs including parts, labor, and client discounts.

---

## Features

- **Order State Machine**: Enforces valid status transitions:  
  `CREATED` → `DIAGNOSED` → `APPROVED` → `IN_PROGRESS` → `COMPLETED` (or `CANCELLED`).
- **Mechanic Assignment**: Checks mechanic availability and required specialization before assigning them to an approved order.
- **Price Calculation**: Calculates total labor and spare parts costs, applies individual client discounts, and handles minimum order thresholds.
- **Detailed Invoice**: Generates a line-by-line price breakdown (`PriceBreakdown`) with base cost, discounts, and the final sum.
- **Validation**: Prevents invalid actions (e.g. modifying a completed order or skipping workflow steps).

---

## Tech Stack

- Java 21
- Maven
- JUnit 5 & Mockito (unit and parameterized tests)
- Lombok

---

## Project Structure

```text
src/main/java/
├── model/          # Car, Client, Mechanic, SparePart, WorkItem, RepairOrder
├── service/        # RepairOrderService (order lifecycle and operations)
├── pricing/        # StandardPricingPolicy, PriceBreakdown
└── repository/     # InMemoryRepairOrderRepository
```
## Getting Started

### Prerequisites
- JDK 21
- Apache Maven 3.8+

### Run Tests
```bash
mvn clean test
```