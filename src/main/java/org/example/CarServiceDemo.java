package org.example;

import java.io.PrintStream;
import java.math.BigDecimal;

public final class CarServiceDemo {
    static void main() {
        PrintStream output = System.out;

        printHeader(output, "CAR SERVICE DOMAIN MODEL DEMO");
        runPositiveScenario(output);
        runApproveWithoutWorksScenario(output);
        runStartWithoutMechanicScenario(output);
        runAssignBusyMechanicScenario(output);
        runIsCompletedOrderScenario(output);
        runIsCanceledOrderScenario(output);
        runUnauthorizedTransitionScenario(output);
    }

    private static void runPositiveScenario(PrintStream output) {
        printHeader(output, "1. POSITIVE SCENARIO");

        ServiceStorage serviceStorage = new ServiceStorage();

        Client client = new Client(
                "C-001",
                "Ivan Petrenko",
                "+380501112233"
        );

        Car car = new Car(
                "WAUZZZ8K9DA123456",
                "Audi",
                "A4",
                2018,
                client
        );

        Mechanic mechanic = new Mechanic(
                "M-001",
                "Oleh Shevchenko",
                MechanicSpecialization.ENGINE
        );

        serviceStorage.registerCar(car);
        output.println("Client and car registered.");

        serviceStorage.registerMechanic(mechanic);
        output.println("Mechanic registered.");

        Order order = serviceStorage.createOrder(
                "O-001",
                car
        );
        printState(output, "Order created", order);

        WorkItem engineDiagnostics = new WorkItem(
                "Engine diagnostics",
                new BigDecimal("0.00"),
                new BigDecimal("900.00")
        );

        WorkItem oilReplacement = new WorkItem(
                "Oil and filter replacement",
                new BigDecimal("1800.00"),
                new BigDecimal("700.00")
        );

        order.diagnose();
        printState(output, "After diagnosis", order);

        order.addWork(engineDiagnostics);
        output.println("Work added: Engine diagnostics.");

        order.addWork(oilReplacement);
        output.println("Work added: Oil and filter replacement.");

        order.approve();
        printState(output, "After approval", order);

        serviceStorage.assignMechanicToOrder(
                order,
                mechanic
        );
        output.println("Mechanic assigned: Oleh Shevchenko.");

        serviceStorage.startOrderProgress(order);
        printState(output, "After work start", order);

        engineDiagnostics.completeWork();
        output.println("Work completed: Engine diagnostics.");

        oilReplacement.completeWork();
        output.println("Work completed: Oil and filter replacement.");

        order.complete();
        printState(output, "After order completion", order);

        BigDecimal finalCost = order.calculateTotalCost();
        String statusMessage = order.buildStatusMessage();

        output.println("Positive scenario completed successfully.");
        output.println(statusMessage);
        output.println("Final cost: " + finalCost + " UAH");
    }

    private static void runApproveWithoutWorksScenario(PrintStream output) {
        printHeader(output, "2. NEGATIVE: APPROVE WITHOUT WORKS");

        try {
            ServiceStorage serviceStorage = new ServiceStorage();
            Client client = createClient("C-002");
            Car car = createCar("WAUZZZ8K9DA223456", client);

            serviceStorage.registerCar(car);

            Order order = serviceStorage.createOrder(
                    "O-002",
                    car
            );

            printState(output, "Order created", order);

            order.diagnose();
            printState(output, "After diagnosis", order);

            output.println("Trying to approve without any work items...");
            order.approve();
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static void runStartWithoutMechanicScenario(PrintStream output) {
        printHeader(output, "3. NEGATIVE: START WITHOUT MECHANIC");

        try {
            ServiceStorage serviceStorage = new ServiceStorage();
            Client client = createClient("C-003");
            Car car = createCar("WAUZZZ8K9DA323456", client);

            serviceStorage.registerCar(car);

            Order order = serviceStorage.createOrder(
                    "O-003",
                    car
            );

            WorkItem diagnostics = new WorkItem(
                    "Computer diagnostics",
                    new BigDecimal("0.00"),
                    new BigDecimal("600.00")
            );

            order.diagnose();
            order.addWork(diagnostics);
            order.approve();
            printState(output, "Approved order without assigned mechanic", order);

            output.println("Trying to start order progress...");
            serviceStorage.startOrderProgress(order);
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static void runAssignBusyMechanicScenario(PrintStream output) {
        printHeader(output, "4. NEGATIVE: ASSIGN BUSY MECHANIC");

        try {
            ServiceStorage serviceStorage = new ServiceStorage();
            Client client = createClient("C-004");
            Car car = createCar("WAUZZZ8K9DA423456", client);
            Mechanic mechanic = createMechanic("M-004");

            serviceStorage.registerCar(car);
            serviceStorage.registerMechanic(mechanic);

            Order activeOrder = createApprovedOrder(
                    serviceStorage,
                    "O-004-A",
                    car,
                    "Engine repair"
            );

            serviceStorage.assignMechanicToOrder(
                    activeOrder,
                    mechanic
            );

            serviceStorage.startOrderProgress(activeOrder);
            printState(output, "First order is active", activeOrder);

            Order secondOrder = createApprovedOrder(
                    serviceStorage,
                    "O-004-B",
                    car,
                    "Suspension check"
            );

            printState(output, "Second order is ready for mechanic", secondOrder);

            output.println("Trying to assign the same busy mechanic...");
            serviceStorage.assignMechanicToOrder(
                    secondOrder,
                    mechanic
            );
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static void runIsCompletedOrderScenario(PrintStream output) {
        printHeader(output, "5. NEGATIVE: IS COMPLETED ORDER IMMUTABLE");

        try {
            ServiceStorage serviceStorage = new ServiceStorage();
            Client client = createClient("C-005");
            Car car = createCar("WAUZZZ8K9DA523456", client);
            Mechanic mechanic = createMechanic("M-005");

            serviceStorage.registerCar(car);
            serviceStorage.registerMechanic(mechanic);

            Order order = createCompletedOrder(
                    serviceStorage,
                    "O-005",
                    car,
                    mechanic
            );

            printState(output, "Completed order", order);

            output.println("Trying to cancel an already completed order...");
            order.cancel();
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static void runIsCanceledOrderScenario(PrintStream output) {
        printHeader(output, "6. NEGATIVE: IS CANCELED ORDER IMMUTABLE");

        try {
            ServiceStorage serviceStorage = new ServiceStorage();
            Client client = createClient("C-006");
            Car car = createCar("WAUZZZ8K9DA623456", client);

            serviceStorage.registerCar(car);

            Order order = serviceStorage.createOrder(
                    "O-006",
                    car
            );

            printState(output, "Order before cancellation", order);

            order.cancel();
            printState(output, "Canceled order", order);

            WorkItem work = new WorkItem(
                    "Body polishing",
                    new BigDecimal("300.00"),
                    new BigDecimal("400.00")
            );

            output.println("Trying to add work to a canceled order...");
            order.addWork(work);
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static void runUnauthorizedTransitionScenario(PrintStream output) {
        printHeader(output, "7. NEGATIVE: UNAUTHORIZED STATUS TRANSITION");

        try {
            ServiceStorage serviceStorage = new ServiceStorage();
            Client client = createClient("C-007");
            Car car = createCar("WAUZZZ8K9DA723456", client);

            serviceStorage.registerCar(car);

            Order order = serviceStorage.createOrder(
                    "O-007",
                    car
            );

            printState(output, "Order is still created", order);

            output.println("Trying to complete immediately from CREATED...");
            order.complete();
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static Order createApprovedOrder(
            ServiceStorage serviceStorage,
            String orderId,
            Car car,
            String workDescription
    ) {
        Order order = serviceStorage.createOrder(
                orderId,
                car
        );

        WorkItem work = new WorkItem(
                workDescription,
                new BigDecimal("500.00"),
                new BigDecimal("700.00")
        );

        order.diagnose();
        order.addWork(work);
        order.approve();

        return order;
    }

    @SuppressWarnings("SameParameterValue")
    private static Order createCompletedOrder(
            ServiceStorage serviceStorage,
            String orderId,
            Car car,
            Mechanic mechanic
    ) {
        Order order = createApprovedOrder(
                serviceStorage,
                orderId,
                car,
                "Planned maintenance"
        );

        serviceStorage.assignMechanicToOrder(
                order,
                mechanic
        );

        serviceStorage.startOrderProgress(order);

        for (WorkItem work : order.getWorks()) {
            work.completeWork();
        }

        order.complete();

        return order;
    }

    private static Client createClient(String id) {
        return new Client(
                id,
                "Test Client",
                "+380501112233"
        );
    }

    private static Car createCar(String vin, Client client) {
        return new Car(
                vin,
                "Audi",
                "A4",
                2018,
                client
        );
    }

    private static Mechanic createMechanic(String id) {
        return new Mechanic(
                id,
                "Test Mechanic",
                MechanicSpecialization.GENERAL
        );
    }

    private static void printHeader(PrintStream output, String title) {
        output.println();
        output.println("============================================================");
        output.println(title);
        output.println("============================================================");
    }

    private static void printState(PrintStream output, String label, Order order) {
        OrderStatus status = order.getStatus();

        output.println(label + ": " + status);
    }

    private static void printError(PrintStream output, RuntimeException exception) {
        String message = exception.getMessage();

        output.println("Expected error: " + message);
        output.println("Application continues.");
    }
}