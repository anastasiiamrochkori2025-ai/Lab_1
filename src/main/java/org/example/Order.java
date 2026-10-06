package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {
    private final String id;
    private final Car car;
    private final List<WorkItem> works;
    private final Mechanic mechanic;
    private final OrderStatus status;

    public Order(String id, Car car) {
        this(id, car, new ArrayList<>(), null, OrderStatus.CREATED);
    }

    public Order(String id, Car car, List<WorkItem> works, Mechanic mechanic, OrderStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id cannot be null or blank.");
        }
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null.");
        }
        if (works == null) {
            throw new IllegalArgumentException("Works list cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null.");
        }

        this.id = id;
        this.car = car;
        this.works = new ArrayList<>(works);
        this.mechanic = mechanic;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public List<WorkItem> getWorks() {
        return Collections.unmodifiableList(works);
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public OrderStatus getStatus() {
        return status;
    }
}