package org.example;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        var output = System.out;
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
        WorkItem diagnostics = new WorkItem("Engine diagnostics", new BigDecimal("0.00"), new BigDecimal("900.00"));
        WorkItem oilChange = new WorkItem("Oil and filter replacement", new BigDecimal("1800.00"), new BigDecimal("700.00"));

        order.diagnose();
        order.addWork(diagnostics);
        order.addWork(oilChange);
        order.approve();
        serviceStorage.assignMechanicToOrder(order, engineMechanic);
        serviceStorage.startOrderProgress(order);

        demonstrateCompletionWithPendingWorks(order);

        output.println("Work: " + diagnostics.getDescription() + " (parts: " + diagnostics.getPartsCost() + ", labor: " + diagnostics.getLaborCost() + ") - " + diagnostics.getStatus());
        diagnostics.completeWork();
        oilChange.completeWork();
        order.complete();

        output.println(order.buildStatusMessage());
        output.println("Car: " + order.getCar().getMake() + " " + order.getCar().getModel() + ", Year: " + order.getCar().getYear());
        output.println("Owner ID: " + order.getCar().getOwner().getId() + ", Name: " + order.getCar().getOwner().getName() + " (" + order.getCar().getOwner().getPhone() + ")");
        output.println("Mechanic: " + order.getMechanic().getName() + " [" + order.getMechanic().getSpecialization() + "]");
        output.println("Works count: " + order.getWorks().size());
        output.println("Current status: " + order.getStatus());
        output.println("Total cost: " + order.calculateTotalCost() + " UAH");

        output.println("Registered cars in service: " + serviceStorage.getRegisteredCars().size());
        output.println("Registered mechanics in service: " + serviceStorage.getMechanics().size());
        output.println("Total orders in storage: " + serviceStorage.getOrders().size());

        demonstrateCancellation(serviceStorage, car);
    }

    private static void demonstrateCancellation(ServiceStorage serviceStorage, Car car) {
        var output = System.out;
        Order cancelOrder = serviceStorage.createOrder("O-CANCEL-1", car);
        cancelOrder.cancel();
        output.println("Cancellation demo: Order " + cancelOrder.getId() + " is now " + cancelOrder.getStatus());
    }

    private static void demonstrateApprovalWithoutWorks(ServiceStorage serviceStorage, Car car) {
        var output = System.out;
        try {
            Order invalidOrder = serviceStorage.createOrder("O-INVALID-1", car);
            invalidOrder.diagnose();
            invalidOrder.approve();
        } catch (IllegalStateException exception) {
            output.println("Approval protection: " + exception.getMessage());
        }
    }

    private static void demonstrateStartWithoutMechanic(ServiceStorage serviceStorage, Car car) {
        var output = System.out;
        try {
            Order invalidOrder = serviceStorage.createOrder("O-INVALID-2", car);
            invalidOrder.diagnose();
            WorkItem brakeInspection = new WorkItem("Brake inspection", new BigDecimal("0.00"), new BigDecimal("500.00"));
            invalidOrder.addWork(brakeInspection);
            invalidOrder.approve();
            serviceStorage.startOrderProgress(invalidOrder);
        } catch (IllegalStateException exception) {
            output.println("Start protection: " + exception.getMessage());
        }
    }

    private static void demonstrateCompletionWithPendingWorks(Order order) {
        var output = System.out;
        try {
            order.complete();
        } catch (IllegalStateException exception) {
            output.println("Completion protection: " + exception.getMessage());
        }
    }

    private static void demonstrateDuplicateVin(ServiceStorage serviceStorage, Client client) {
        var output = System.out;
        try {
            Car duplicateCar = new Car("WAUZZZ8K9DA123456", "Audi", "A4", 2018, client);
            serviceStorage.registerCar(duplicateCar);
        } catch (IllegalArgumentException exception) {
            output.println("VIN protection: " + exception.getMessage());
        }
    }
}
