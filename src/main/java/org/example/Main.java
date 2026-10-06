package org.example;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

public final class Main {
    public static final void main(String[] args) {
        PrintStream output = System.out;
        ServiceStorage serviceStorage = new ServiceStorage();

        Client client = new Client("C-001", "Ivan Petrenko", "+380501112233");
        Car car = new Car("WAUZZZ8K9DA123456", "Audi", "A4", 2018, client);

        Mechanic engineMechanic = new Mechanic("M-001", "Oleh Shevchenko", MechanicSpecialization.ENGINE);
        Mechanic diagMechanic = new Mechanic("M-002", "Vasyl Sydorenko", MechanicSpecialization.DIAGNOSTICS);
        Mechanic electricMechanic = new Mechanic("M-003", "Andrii Kravets", MechanicSpecialization.ELECTRICAL);
        Mechanic generalMechanic = new Mechanic("M-004", "Dmytro Boyko", MechanicSpecialization.GENERAL);

        serviceStorage.registerCar(car);
        serviceStorage.registerMechanic(engineMechanic);
        serviceStorage.registerMechanic(diagMechanic);
        serviceStorage.registerMechanic(electricMechanic);
        serviceStorage.registerMechanic(generalMechanic);

        demonstrateApprovalWithoutWorks(serviceStorage, car);
        demonstrateStartWithoutMechanic(serviceStorage, car);
        demonstrateDuplicateVin(serviceStorage, client);

        Order order = serviceStorage.createOrder("O-001", car);
        BigDecimal diagParts = new BigDecimal("0.00");
        BigDecimal diagLabor = new BigDecimal("900.00");
        WorkItem diagnostics = new WorkItem("Engine diagnostics", diagParts, diagLabor);

        BigDecimal oilParts = new BigDecimal("1800.00");
        BigDecimal oilLabor = new BigDecimal("700.00");
        WorkItem oilChange = new WorkItem("Oil and filter replacement", oilParts, oilLabor);

        order = serviceStorage.diagnoseOrder(order);
        order = serviceStorage.addWorkToOrder(order, diagnostics);
        order = serviceStorage.addWorkToOrder(order, oilChange);
        order = serviceStorage.approveOrder(order);
        order = serviceStorage.assignMechanicToOrder(order, engineMechanic);
        order = serviceStorage.startOrderProgress(order);

        demonstrateCompletionWithPendingWorks(serviceStorage, order);

        String diagDesc = diagnostics.getDescription();
        BigDecimal currentDiagParts = diagnostics.getPartsCost();
        BigDecimal currentDiagLabor = diagnostics.getLaborCost();
        WorkStatus diagStatus = diagnostics.getStatus();
        output.println("Work: " + diagDesc + " (parts: " + currentDiagParts + ", labor: " + currentDiagLabor + ") - " + diagStatus);

        diagnostics = serviceStorage.completeWork(diagnostics);
        oilChange = serviceStorage.completeWork(oilChange);
        order = serviceStorage.completeOrder(order);

        String orderId = order.getId();
        OrderStatus finalStatus = order.getStatus();
        output.println("Order " + orderId + " status: " + finalStatus);

        Car orderCar = order.getCar();
        String make = orderCar.getMake();
        String model = orderCar.getModel();
        int year = orderCar.getYear();
        output.println("Car: " + make + " " + model + ", Year: " + year);

        Client owner = orderCar.getOwner();
        String ownerId = owner.getId();
        String ownerName = owner.getName();
        String ownerPhone = owner.getPhone();
        output.println("Owner ID: " + ownerId + ", Name: " + ownerName + " (" + ownerPhone + ")");

        Mechanic mechanicFromOrder = order.getMechanic();
        String mechanicName = mechanicFromOrder.getName();
        MechanicSpecialization spec = mechanicFromOrder.getSpecialization();
        output.println("Mechanic: " + mechanicName + " [" + spec + "]");

        List<WorkItem> worksList = order.getWorks();
        int worksCount = worksList.size();
        output.println("Works count: " + worksCount);

        OrderStatus orderStatus = order.getStatus();
        output.println("Current status: " + orderStatus);

        BigDecimal totalCost = serviceStorage.calculateTotalCost(order);
        output.println("Total cost: " + totalCost + " UAH");

        List<Car> registeredCars = serviceStorage.getRegisteredCars();
        int registeredCarsCount = registeredCars.size();
        output.println("Registered cars in service: " + registeredCarsCount);

        List<Mechanic> mechanicsList = serviceStorage.getMechanics();
        int mechanicsCount = mechanicsList.size();
        output.println("Registered mechanics in service: " + mechanicsCount);

        List<Order> ordersList = serviceStorage.getOrders();
        int ordersCount = ordersList.size();
        output.println("Total orders in storage: " + ordersCount);

        demonstrateCancellation(serviceStorage, car);

        CarServiceDemo.main();
    }

    private static final void demonstrateCancellation(ServiceStorage serviceStorage, Car car) {
        PrintStream output = System.out;
        Order cancelOrder = serviceStorage.createOrder("O-CANCEL-1", car);
        cancelOrder = serviceStorage.cancelOrder(cancelOrder);
        String id = cancelOrder.getId();
        OrderStatus status = cancelOrder.getStatus();
        output.println("Cancellation demo: Order " + id + " is now " + status);
    }

    private static final void demonstrateApprovalWithoutWorks(ServiceStorage serviceStorage, Car car) {
        PrintStream output = System.out;
        try {
            Order invalidOrder = serviceStorage.createOrder("O-INVALID-1", car);
            invalidOrder = serviceStorage.diagnoseOrder(invalidOrder);
            serviceStorage.approveOrder(invalidOrder);
        } catch (IllegalStateException exception) {
            String message = exception.getMessage();
            output.println("Approval protection: " + message);
        }
    }

    private static final void demonstrateStartWithoutMechanic(ServiceStorage serviceStorage, Car car) {
        PrintStream output = System.out;
        try {
            Order invalidOrder = serviceStorage.createOrder("O-INVALID-2", car);
            invalidOrder = serviceStorage.diagnoseOrder(invalidOrder);
            BigDecimal partsCost = new BigDecimal("0.00");
            BigDecimal laborCost = new BigDecimal("500.00");
            WorkItem brakeInspection = new WorkItem("Brake inspection", partsCost, laborCost);
            invalidOrder = serviceStorage.addWorkToOrder(invalidOrder, brakeInspection);
            invalidOrder = serviceStorage.approveOrder(invalidOrder);
            serviceStorage.startOrderProgress(invalidOrder);
        } catch (IllegalStateException exception) {
            String message = exception.getMessage();
            output.println("Start protection: " + message);
        }
    }

    private static final void demonstrateCompletionWithPendingWorks(ServiceStorage serviceStorage, Order order) {
        PrintStream output = System.out;
        try {
            serviceStorage.completeOrder(order);
        } catch (IllegalStateException exception) {
            String message = exception.getMessage();
            output.println("Completion protection: " + message);
        }
    }

    private static final void demonstrateDuplicateVin(ServiceStorage serviceStorage, Client client) {
        PrintStream output = System.out;
        try {
            Car duplicateCar = new Car("WAUZZZ8K9DA123456", "Audi", "A4", 2018, client);
            serviceStorage.registerCar(duplicateCar);
        } catch (IllegalArgumentException exception) {
            String message = exception.getMessage();
            output.println("VIN protection: " + message);
        }
    }
}

final class CarServiceDemo {
    public static final void main() {
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

    private static final void runPositiveScenario(PrintStream output) {
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

        BigDecimal diagParts = new BigDecimal("0.00");
        BigDecimal diagLabor = new BigDecimal("900.00");
        WorkItem engineDiagnostics = new WorkItem(
                "Engine diagnostics",
                diagParts,
                diagLabor
        );

        BigDecimal oilParts = new BigDecimal("1800.00");
        BigDecimal oilLabor = new BigDecimal("700.00");
        WorkItem oilReplacement = new WorkItem(
                "Oil and filter replacement",
                oilParts,
                oilLabor
        );

        order = serviceStorage.diagnoseOrder(order);
        printState(output, "After diagnosis", order);

        order = serviceStorage.addWorkToOrder(order, engineDiagnostics);
        output.println("Work added: Engine diagnostics.");

        order = serviceStorage.addWorkToOrder(order, oilReplacement);
        output.println("Work added: Oil and filter replacement.");

        order = serviceStorage.approveOrder(order);
        printState(output, "After approval", order);

        order = serviceStorage.assignMechanicToOrder(
                order,
                mechanic
        );
        output.println("Mechanic assigned: Oleh Shevchenko.");

        order = serviceStorage.startOrderProgress(order);
        printState(output, "After work start", order);

        engineDiagnostics = serviceStorage.completeWork(engineDiagnostics);
        output.println("Work completed: Engine diagnostics.");

        oilReplacement = serviceStorage.completeWork(oilReplacement);
        output.println("Work completed: Oil and filter replacement.");

        order = serviceStorage.completeOrder(order);
        printState(output, "After order completion", order);

        BigDecimal finalCost = serviceStorage.calculateTotalCost(order);
        String orderId = order.getId();
        OrderStatus finalStatus = order.getStatus();

        output.println("Positive scenario completed successfully.");
        output.println("Order " + orderId + " status: " + finalStatus);
        output.println("Final cost: " + finalCost + " UAH");
    }

    private static final void runApproveWithoutWorksScenario(PrintStream output) {
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

            order = serviceStorage.diagnoseOrder(order);
            printState(output, "After diagnosis", order);

            output.println("Trying to approve without any work items...");
            serviceStorage.approveOrder(order);
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static final void runStartWithoutMechanicScenario(PrintStream output) {
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

            BigDecimal parts = new BigDecimal("0.00");
            BigDecimal labor = new BigDecimal("600.00");
            WorkItem diagnostics = new WorkItem(
                    "Computer diagnostics",
                    parts,
                    labor
            );

            order = serviceStorage.diagnoseOrder(order);
            order = serviceStorage.addWorkToOrder(order, diagnostics);
            order = serviceStorage.approveOrder(order);
            printState(output, "Approved order without assigned mechanic", order);

            output.println("Trying to start order progress...");
            serviceStorage.startOrderProgress(order);
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static final void runAssignBusyMechanicScenario(PrintStream output) {
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

            activeOrder = serviceStorage.assignMechanicToOrder(
                    activeOrder,
                    mechanic
            );

            activeOrder = serviceStorage.startOrderProgress(activeOrder);
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

    private static final void runIsCompletedOrderScenario(PrintStream output) {
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
            serviceStorage.cancelOrder(order);
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static final void runIsCanceledOrderScenario(PrintStream output) {
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

            order = serviceStorage.cancelOrder(order);
            printState(output, "Canceled order", order);

            BigDecimal parts = new BigDecimal("300.00");
            BigDecimal labor = new BigDecimal("400.00");
            WorkItem work = new WorkItem(
                    "Body polishing",
                    parts,
                    labor
            );

            output.println("Trying to add work to a canceled order...");
            serviceStorage.addWorkToOrder(order, work);
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static final void runUnauthorizedTransitionScenario(PrintStream output) {
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
            serviceStorage.completeOrder(order);
        } catch (RuntimeException exception) {
            printError(output, exception);
        }
    }

    private static final Order createApprovedOrder(
            ServiceStorage serviceStorage,
            String orderId,
            Car car,
            String workDescription
    ) {
        Order order = serviceStorage.createOrder(
                orderId,
                car
        );

        BigDecimal parts = new BigDecimal("500.00");
        BigDecimal labor = new BigDecimal("700.00");
        WorkItem work = new WorkItem(
                workDescription,
                parts,
                labor
        );

        order = serviceStorage.diagnoseOrder(order);
        order = serviceStorage.addWorkToOrder(order, work);
        order = serviceStorage.approveOrder(order);

        return order;
    }

    private static final Order createCompletedOrder(
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

        order = serviceStorage.assignMechanicToOrder(
                order,
                mechanic
        );

        order = serviceStorage.startOrderProgress(order);

        List<WorkItem> works = order.getWorks();
        for (WorkItem work : works) {
            serviceStorage.completeWork(work);
        }

        order = serviceStorage.completeOrder(order);

        return order;
    }

    private static final Client createClient(String id) {
        return new Client(
                id,
                "Test Client",
                "+380501112233"
        );
    }

    private static final Car createCar(String vin, Client client) {
        return new Car(
                vin,
                "Audi",
                "A4",
                2018,
                client
        );
    }

    private static final Mechanic createMechanic(String id) {
        return new Mechanic(
                id,
                "Test Mechanic",
                MechanicSpecialization.GENERAL
        );
    }

    private static final void printHeader(PrintStream output, String title) {
        output.println();
        output.println("============================================================");
        output.println(title);
        output.println("============================================================");
    }

    private static final void printState(PrintStream output, String label, Order order) {
        OrderStatus status = order.getStatus();

        output.println(label + ": " + status);
    }

    private static final void printError(PrintStream output, RuntimeException exception) {
        String message = exception.getMessage();

        output.println("Expected error: " + message);
        output.println("Application continues.");
    }
}
