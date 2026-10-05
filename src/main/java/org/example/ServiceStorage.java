package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ServiceStorage {
    private final List<Car> cars;
    private final List<Mechanic> mechanics;
    private final List<Order> orders;

    public ServiceStorage() {
        this.cars = new ArrayList<>();
        this.mechanics = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    public void registerCar(Car car) {
        Car newCar = requireNotNull(car, "Car cannot be null.");
        if (hasRegisteredCar(newCar)) {
            throw new IllegalArgumentException("Car with VIN " + newCar.getVin() + " is already registered.");
        }
        cars.add(newCar);
    }

    public void registerMechanic(Mechanic mechanic) {
        Mechanic newMechanic = requireNotNull(mechanic, "Mechanic cannot be null.");
        if (hasRegisteredMechanic(newMechanic)) {
            throw new IllegalArgumentException("Mechanic with id " + newMechanic.getId() + " is already registered.");
        }
        mechanics.add(newMechanic);
    }

    public Order createOrder(String orderId, Car car) {
        String checkedOrderId = requireOrderId(orderId);
        Car checkedCar = requireNotNull(car, "Car cannot be null.");

        if (!hasRegisteredCar(checkedCar)) {
            throw new IllegalStateException("Car must be registered before creating an order.");
        }
        if (hasOrderWithId(checkedOrderId)) {
            throw new IllegalArgumentException("Order with id " + checkedOrderId + " already exists.");
        }

        Order order = new Order(checkedOrderId, checkedCar);
        orders.add(order);
        return order;
    }

    public void assignMechanicToOrder(Order order, Mechanic mechanic) {
        Order registeredOrder = requireRegisteredOrder(order);
        Mechanic registeredMechanic = requireRegisteredMechanic(mechanic);

        registeredOrder.assignMechanic(registeredMechanic);
    }

    public void startOrderProgress(Order order) {
        Order registeredOrder = requireRegisteredOrder(order);

        if (hasAnotherActiveOrderFor(registeredOrder)) {
            throw new IllegalStateException("Mechanic cannot have two active orders at the same time.");
        }

        registeredOrder.startProgress();
    }

    public List<Car> getRegisteredCars() {
        List<Car> registeredCars = new ArrayList<>(cars);
        return Collections.unmodifiableList(registeredCars);
    }

    public List<Mechanic> getMechanics() {
        List<Mechanic> registeredMechanics = new ArrayList<>(mechanics);
        return Collections.unmodifiableList(registeredMechanics);
    }

    public List<Order> getOrders() {
        List<Order> registeredOrders = new ArrayList<>(orders);
        return Collections.unmodifiableList(registeredOrders);
    }

    private boolean hasRegisteredCar(Car car) {
        for (Car registeredCar : cars) {
            if (registeredCar.hasSameVin(car)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRegisteredMechanic(Mechanic mechanic) {
        for (Mechanic registeredMechanic : mechanics) {
            if (registeredMechanic.hasSameId(mechanic)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOrderWithId(String orderId) {
        for (Order registeredOrder : orders) {
            if (registeredOrder.getId().equals(orderId)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnotherActiveOrderFor(Order order) {
        if (!order.hasAssignedMechanic()) {
            return false;
        }

        for (Order registeredOrder : orders) {
            if (registeredOrder.isAnotherActiveOrderFor(order)) {
                return true;
            }
        }
        return false;
    }

    private Order requireRegisteredOrder(Order order) {
        Order checkedOrder = requireNotNull(order, "Order cannot be null.");

        for (Order registeredOrder : orders) {
            if (registeredOrder.isRegisteredObject(checkedOrder)) {
                return registeredOrder;
            }
        }
        throw new IllegalStateException("Order must be registered in this service storage.");
    }

    private Mechanic requireRegisteredMechanic(Mechanic mechanic) {
        Mechanic checkedMechanic = requireNotNull(mechanic, "Mechanic cannot be null.");

        for (Mechanic registeredMechanic : mechanics) {
            if (registeredMechanic == checkedMechanic) {
                return registeredMechanic;
            }
        }
        throw new IllegalStateException("Mechanic must be registered in this service storage.");
    }

    private static String requireOrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order id cannot be null or blank.");
        }
        return value;
    }

    private static <T> T requireNotNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}