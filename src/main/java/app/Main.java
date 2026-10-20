package app;

import domain.Car;
import domain.Client;
import domain.Mechanic;
import domain.MechanicSpecialization;
import domain.RepairOrder;
import domain.SparePart;
import domain.WorkItem;
import pricing.PricingPolicy;
import pricing.StandardPricingPolicy;
import repository.InMemoryRepairOrderRepository;
import repository.RepairOrderRepository;
import service.OrderCheckoutResult;
import service.RepairOrderService;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        RepairOrderRepository repository = new InMemoryRepairOrderRepository();
        PricingPolicy pricingPolicy = new StandardPricingPolicy(new BigDecimal("300.00"));
        RepairOrderService service = new RepairOrderService(repository, pricingPolicy);
        ReceiptPrinter printer = new ReceiptPrinter(System.out);

        Mechanic general = new Mechanic("M-1", "John Smith", MechanicSpecialization.GENERAL);
        Mechanic electrician = new Mechanic("M-2", "Elena Green", MechanicSpecialization.ELECTRICAL);

        Client melnyk = new Client("C-101", "Alex Melnyk", "+380501234567", new BigDecimal("10.00"));
        Client shevchenko = new Client("C-102", "Mary Shevchenko", "+380677654321");

        scenarioFullLifecycleWithDiscount(service, printer, melnyk, general);
        scenarioMinimumPrice(service, printer, shevchenko, electrician);
        scenarioCancelFreesMechanic(service, melnyk, general);
        scenarioOrdersByClient(service, melnyk);
        scenarioMissingOrder(service);
        scenarioInvalidOperations(service, shevchenko, electrician);
    }

    // Scenario 1: full lifecycle, 10% discount. Expected: base 5150.00, discount 515.00, total to pay 4635.00.
    private static void scenarioFullLifecycleWithDiscount(RepairOrderService service, ReceiptPrinter printer,
                                                          Client client, Mechanic mechanic) {
        title("Scenario 1: Full lifecycle with a personal discount");

        Car car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2020, client);
        WorkItem diagnostics = new WorkItem("Computer diagnostics",
                new BigDecimal("500.00"), MechanicSpecialization.DIAGNOSTICS);
        WorkItem brakes = new WorkItem("Front brake pad replacement",
                new BigDecimal("600.00"), MechanicSpecialization.GENERAL,
                List.of(new SparePart("BP-204", "Front brake pads", "Brembo", new BigDecimal("1850.00"))));
        WorkItem oil = new WorkItem("Engine oil and filter change",
                new BigDecimal("400.00"), MechanicSpecialization.ENGINE,
                List.of(new SparePart("OIL-5W30", "Engine oil 5W-30 (4L)", "Motul", new BigDecimal("1450.00")),
                        new SparePart("OF-112", "Oil filter", "Mann", new BigDecimal("350.00"))));

        String id = "RO-2026-001";
        service.saveOrder(new RepairOrder(id, car));
        service.diagnose(id);
        service.addWork(id, diagnostics);
        service.addWork(id, brakes);
        service.addWork(id, oil);
        service.approve(id);
        service.assignMechanic(id, mechanic);
        service.startProgress(id);
        service.completeWork(id, diagnostics);
        service.completeWork(id, brakes);
        service.completeWork(id, oil);

        OrderCheckoutResult result = service.checkout(id);
        printer.print(result);
    }

    // Scenario 2: low total and client with no discount. Expected: base 120.00, minimum threshold -> 300.00.
    private static void scenarioMinimumPrice(RepairOrderService service, ReceiptPrinter printer,
                                             Client client, Mechanic mechanic) {
        title("Scenario 2: Minimum order price threshold applied");

        Car car = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", 2012, client);
        WorkItem wiring = new WorkItem("Wiring inspection",
                new BigDecimal("120.00"), MechanicSpecialization.ELECTRICAL);

        String id = "RO-2026-002";
        service.saveOrder(new RepairOrder(id, car));
        service.diagnose(id);
        service.addWork(id, wiring);
        service.approve(id);
        service.assignMechanic(id, mechanic);
        service.startProgress(id);
        service.completeWork(id, wiring);

        printer.print(service.checkout(id));
    }

    // Scenario 3: order cancellation frees the mechanic.
    private static void scenarioCancelFreesMechanic(RepairOrderService service, Client client, Mechanic mechanic) {
        title("Scenario 3: Cancelling order frees the mechanic");

        Car car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2020, client);
        String id = "RO-2026-003";
        service.saveOrder(new RepairOrder(id, car));
        service.diagnose(id);
        service.addWork(id, new WorkItem("Air filter replacement", new BigDecimal("250.00")));
        service.approve(id);
        service.assignMechanic(id, mechanic);
        System.out.println("Is mechanic available after assignment? " + mechanic.isAvailable());

        service.cancel(id);
        System.out.println("Order status: " + service.getOrderById(id).getStatus());
        System.out.println("Is mechanic available after cancellation? " + mechanic.isAvailable());
        System.out.println();
    }

    private static void scenarioOrdersByClient(RepairOrderService service, Client client) {
        title("Scenario 4: Search orders for client " + client.getId());
        for (RepairOrder order : service.getOrdersByClientId(client.getId())) {
            System.out.println(order.getId() + " - " + order.getStatus());
        }
        System.out.println("Orders for client C-999 (non-existent client): "
                + service.getOrdersByClientId("C-999").size());
        System.out.println();
    }

    // Behavior for non-existent order: repository returns Optional.empty(),
    // service translates it to NoSuchElementException.
    private static void scenarioMissingOrder(RepairOrderService service) {
        title("Scenario 5: Non-existent order");
        expectFailure("getOrderById(\"NOPE\")", () -> service.getOrderById("NOPE"));
        expectFailure("checkout(\"NOPE\")", () -> service.checkout("NOPE"));
        expectFailure("getOrderById(\" \")", () -> service.getOrderById(" "));
        System.out.println();
    }

    private static void scenarioInvalidOperations(RepairOrderService service, Client client, Mechanic electrician) {
        title("Scenario 6: Invalid operations");

        Car car = new Car("JTDKB20U793000002", "Toyota", "Yaris", 2018, client);

        String emptyId = "RO-2026-004";
        service.saveOrder(new RepairOrder(emptyId, car));
        service.diagnose(emptyId);
        expectFailure("approving order with no work items", () -> service.approve(emptyId));

        String engineId = "RO-2026-005";
        service.saveOrder(new RepairOrder(engineId, car));
        service.diagnose(engineId);
        service.addWork(engineId, new WorkItem("Engine repair", new BigDecimal("3000.00"), MechanicSpecialization.ENGINE));
        service.approve(engineId);
        expectFailure("assigning electrician to an engine repair job", () -> service.assignMechanic(engineId, electrician));
        expectFailure("checkout on an order not in progress", () -> service.checkout(engineId));
        System.out.println("Status after failed checkout: " + service.getOrderById(engineId).getStatus());
        System.out.println();
    }

    private static void title(String text) {
        System.out.println("### " + text);
    }

    private static void expectFailure(String description, Runnable action) {
        try {
            action.run();
            System.out.println("[ERROR] Expected an exception for: " + description);
        } catch (RuntimeException e) {
            System.out.println(description + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}