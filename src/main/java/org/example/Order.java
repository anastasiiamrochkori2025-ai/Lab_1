package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {
    private final String id;
    private final Car car;
    private final List<WorkItem> works;
    private Mechanic mechanic;
    private OrderStatus status;

    public Order(String id, Car car) {
        this.id = requireId(id);
        this.car = requireNotNull(car, "Order car cannot be null.");
        this.works = new ArrayList<>();
        this.status = OrderStatus.CREATED;
    }

    public String getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public List<WorkItem> getWorks() {
        List<WorkItem> worksCopy = new ArrayList<>(works);
        return Collections.unmodifiableList(worksCopy);
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isRegisteredObject(Order order) {
        return this == order;
    }

    public boolean isAssignedTo(Mechanic mechanic) {
        Mechanic checkedMechanic = requireNotNull(mechanic, "Mechanic cannot be null.");
        return this.mechanic == checkedMechanic;
    }

    public boolean isAnotherActiveOrderFor(Order order) {
        Order checkedOrder = requireNotNull(order, "Order cannot be null.");

        if (this == checkedOrder) {
            return false;
        }

        if (checkedOrder.mechanic == null) {
            return false;
        }

        return isInProgress() && isAssignedTo(checkedOrder.mechanic);
    }

    public boolean hasAssignedMechanic() {
        return mechanic != null;
    }

    public String buildStatusMessage() {
        return "Order " + id + " status: " + status;
    }

    public void addWork(WorkItem work) {
        checkModifiable();
        works.add(requireNotNull(work, "Work item cannot be null."));
    }

    public void diagnose() {
        requireStatus(OrderStatus.CREATED, "Only a created order can be diagnosed.");
        status = OrderStatus.DIAGNOSED;
    }

    public void approve() {
        requireStatus(OrderStatus.DIAGNOSED, "Only a diagnosed order can be approved.");
        if (works.isEmpty()) {
            throw new IllegalStateException("Order cannot be approved without work items.");
        }
        status = OrderStatus.APPROVED;
    }

    public void assignMechanic(Mechanic mechanic) {
        checkModifiable();
        if (status == OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Order is in progress, mechanic reassign is forbidden.");
        }
        Mechanic selectedMechanic = requireNotNull(mechanic, "Mechanic cannot be null.");
        if (selectedMechanic.isBusy()) {
            throw new IllegalStateException("Cannot assign a busy mechanic.");
        }
        this.mechanic = selectedMechanic;
    }

    public void startProgress() {
        requireStatus(OrderStatus.APPROVED, "Only an approved order can be started.");
        if (mechanic == null) {
            throw new IllegalStateException("Order cannot be started without an assigned mechanic.");
        }
        mechanic.assignToOrder();
        status = OrderStatus.IN_PROGRESS;
    }

    public void complete() {
        requireStatus(OrderStatus.IN_PROGRESS, "Only an order in progress can be completed.");
        if (!hasOnlyCompletedWorks()) {
            throw new IllegalStateException("Order cannot be completed until all work items are completed.");
        }
        status = OrderStatus.COMPLETED;
        mechanic.releaseFromOrder();
    }

    public void cancel() {
        if (status != OrderStatus.CREATED && status != OrderStatus.DIAGNOSED && status != OrderStatus.APPROVED) {
            throw new IllegalStateException("Only created, diagnosed, or approved orders can be cancelled.");
        }
        status = OrderStatus.CANCELLED;
        if (mechanic != null) {
            mechanic.releaseFromOrder();
        }
    }

    public BigDecimal calculateTotalCost() {
        BigDecimal totalCost = BigDecimal.ZERO;

        for (WorkItem work : works) {
            BigDecimal workCost = work.getTotalCost();
            totalCost = totalCost.add(workCost);
        }

        return totalCost;
    }

    public boolean isModifiable() {
        return status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED;
    }

    private boolean isInProgress() {
        return status == OrderStatus.IN_PROGRESS;
    }

    private boolean hasOnlyCompletedWorks() {
        for (WorkItem work : works) {
            if (!work.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    private void checkModifiable() {
        if (!isModifiable()) {
            throw new IllegalStateException("Order is closed (completed or cancelled) and cannot be modified.");
        }
    }

    private void requireStatus(OrderStatus requiredStatus, String message) {
        if (status != requiredStatus) {
            throw new IllegalStateException(message);
        }
    }

    private static String requireId(String value) {
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