package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepairOrder {
    private String id;
    private Car car;
    private Client client;
    private List<WorkItem> works;
    private List<SparePart> spareParts;
    private OrderStatus status;

    public RepairOrder(String id, Car car, Client client) {
        this(id, car, client, new ArrayList<>(), new ArrayList<>(), OrderStatus.CREATED);
    }

    public RepairOrder(RepairOrder other) {
        if (other == null) {
            throw new IllegalArgumentException("Repair order cannot be null.");
        }
        this.id = other.id;
        this.car = other.car;
        this.client = other.client;
        this.works = new ArrayList<>(other.works);
        this.spareParts = new ArrayList<>(other.spareParts);
        this.status = other.status;
    }

    public RepairOrder(
            String id,
            Car car,
            Client client,
            List<WorkItem> works,
            List<SparePart> spareParts,
            OrderStatus status
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Repair order id cannot be null or blank.");
        }
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null.");
        }
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null.");
        }
        if (works == null) {
            throw new IllegalArgumentException("Works list cannot be null.");
        }
        if (spareParts == null) {
            throw new IllegalArgumentException("Spare parts list cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null.");
        }

        this.id = id;
        this.car = car;
        this.client = client;
        this.works = new ArrayList<>(works);
        this.spareParts = new ArrayList<>(spareParts);
        this.status = status;
    }

    public void addWork(WorkItem work) {
        if (work == null) {
            throw new IllegalArgumentException("Work item cannot be null.");
        }
        this.works.add(work);
    }

    public void addSparePart(SparePart sparePart) {
        if (sparePart == null) {
            throw new IllegalArgumentException("Spare part cannot be null.");
        }
        this.spareParts.add(sparePart);
    }

    public BigDecimal calculateBasePrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (WorkItem work : works) {
            total = total.add(work.getPartsCost()).add(work.getLaborCost());
        }
        for (SparePart part : spareParts) {
            total = total.add(part.getPrice());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public Client getClient() {
        return client;
    }

    public List<WorkItem> getWorks() {
        return Collections.unmodifiableList(works);
    }

    public List<SparePart> getSpareParts() {
        return Collections.unmodifiableList(spareParts);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null.");
        }
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepairOrder that = (RepairOrder) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "RepairOrder{" +
                "id='" + id + '\'' +
                ", car=" + car.getVin() +
                ", client=" + client.getName() +
                ", worksCount=" + works.size() +
                ", partsCount=" + spareParts.size() +
                ", status=" + status +
                '}';
    }
}
