package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceStorage {
    private List<Car> cars = new ArrayList<>();
    private List<Mechanic> mechanics = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();

    public void registerCar(Car car) {
        ensureNotNull(car, "Car cannot be null.");
        if (hasRegisteredCar(car)) {
            throw new IllegalArgumentException("Car with VIN " + car.getVin() + " is already registered.");
        }
        cars.add(car);
    }

    public void registerMechanic(Mechanic mechanic) {
        ensureNotNull(mechanic, "Mechanic cannot be null.");
        if (hasRegisteredMechanic(mechanic)) {
            throw new IllegalArgumentException("Mechanic with id " + mechanic.getId() + " is already registered.");
        }
        mechanics.add(mechanic);
    }

    public Order createOrder(String orderId, Car car) {
        ensureNotNull(orderId, "Order id cannot be null.");
        if (orderId.isBlank()) {
            throw new IllegalArgumentException("Order id cannot be blank.");
        }
        requireRegisteredCar(car);
        if (hasOrderWithId(orderId)) {
            throw new IllegalArgumentException("Order with id " + orderId + " already exists.");
        }
        Order order = new Order(orderId, car);
        orders.add(order);
        return order;
    }

    public Order addWorkToOrder(Order order, WorkItem work) {
        Order current = requireRegisteredOrder(order);
        ensureNotNull(work, "Work item cannot be null.");
        ensureOrderModifiable(current);

        List<WorkItem> newWorks = new ArrayList<>(current.getWorks());
        newWorks.add(work);

        Order updated = new Order(current.getId(), current.getCar(), newWorks, current.getMechanic(), current.getStatus());
        replaceOrder(current, updated);
        return updated;
    }

    public Order diagnoseOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        ensureOrderStatus(current, OrderStatus.CREATED, "Only a created order can be diagnosed.");
        return updateOrderStatus(current, OrderStatus.DIAGNOSED);
    }

    public Order approveOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        ensureOrderStatus(current, OrderStatus.DIAGNOSED, "Only a diagnosed order can be approved.");
        if (current.getWorks().isEmpty()) {
            throw new IllegalStateException("Order cannot be approved without work items.");
        }
        return updateOrderStatus(current, OrderStatus.APPROVED);
    }

    public Order assignMechanicToOrder(Order order, Mechanic mechanic) {
        Order current = requireRegisteredOrder(order);
        Mechanic registeredMechanic = requireRegisteredMechanic(mechanic);
        ensureOrderModifiable(current);

        if (OrderStatus.IN_PROGRESS.equals(current.getStatus())) {
            throw new IllegalStateException("Order is in progress, mechanic reassign is forbidden.");
        }
        if (isMechanicBusy(registeredMechanic)) {
            throw new IllegalStateException("Cannot assign a busy mechanic.");
        }

        Order updated = new Order(current.getId(), current.getCar(), current.getWorks(), registeredMechanic, current.getStatus());
        replaceOrder(current, updated);
        return updated;
    }

    public Order startOrderProgress(Order order) {
        Order current = requireRegisteredOrder(order);
        ensureOrderStatus(current, OrderStatus.APPROVED, "Only an approved order can be started.");
        if (current.getMechanic() == null) {
            throw new IllegalStateException("Order cannot be started without an assigned mechanic.");
        }
        if (hasAnotherActiveOrderFor(current)) {
            throw new IllegalStateException("Mechanic cannot have two active orders at the same time.");
        }
        return updateOrderStatus(current, OrderStatus.IN_PROGRESS);
    }

    public WorkItem completeWork(WorkItem work) {
        ensureNotNull(work, "Work item cannot be null.");
        if (WorkStatus.COMPLETED.equals(work.getStatus())) {
            throw new IllegalStateException("Work item is already completed.");
        }

        String desc = work.getDescription();
        WorkItem completedWork = new WorkItem(desc, work.getPartsCost(), work.getLaborCost(), WorkStatus.COMPLETED);

        for (Order registeredOrder : orders) {
            List<WorkItem> works = registeredOrder.getWorks();
            boolean contains = false;
            for (WorkItem item : works) {
                if (desc.equals(item.getDescription())) {
                    contains = true;
                    break;
                }
            }
            if (contains) {
                List<WorkItem> newWorks = new ArrayList<>();
                for (WorkItem item : works) {
                    if (desc.equals(item.getDescription())) {
                        newWorks.add(completedWork);
                    } else {
                        newWorks.add(item);
                    }
                }
                Order updatedOrder = new Order(
                        registeredOrder.getId(),
                        registeredOrder.getCar(),
                        newWorks,
                        registeredOrder.getMechanic(),
                        registeredOrder.getStatus()
                );
                replaceOrder(registeredOrder, updatedOrder);
            }
        }
        return completedWork;
    }

    public Order completeOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        ensureOrderStatus(current, OrderStatus.IN_PROGRESS, "Only an order in progress can be completed.");
        for (WorkItem work : current.getWorks()) {
            if (!WorkStatus.COMPLETED.equals(work.getStatus())) {
                throw new IllegalStateException("Order cannot be completed until all work items are completed.");
            }
        }
        return updateOrderStatus(current, OrderStatus.COMPLETED);
    }

    public Order cancelOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        OrderStatus status = current.getStatus();
        boolean canCancel = OrderStatus.CREATED.equals(status)
                || OrderStatus.DIAGNOSED.equals(status)
                || OrderStatus.APPROVED.equals(status);
        if (!canCancel) {
            throw new IllegalStateException("Only created, diagnosed, or approved orders can be cancelled.");
        }
        return updateOrderStatus(current, OrderStatus.CANCELLED);
    }

    public BigDecimal calculateTotalCost(Order order) {
        Order current = requireRegisteredOrder(order);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (WorkItem work : current.getWorks()) {
            totalCost = totalCost.add(work.getPartsCost().add(work.getLaborCost()));
        }
        return totalCost;
    }

    public List<Car> getRegisteredCars() {
        return Collections.unmodifiableList(new ArrayList<>(cars));
    }

    public List<Mechanic> getMechanics() {
        return Collections.unmodifiableList(new ArrayList<>(mechanics));
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    public boolean hasRegisteredCar(Car car) {
        if (car == null) {
            return false;
        }
        String targetVin = car.getVin();
        for (Car registeredCar : cars) {
            if (targetVin.equalsIgnoreCase(registeredCar.getVin())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRegisteredMechanic(Mechanic mechanic) {
        if (mechanic == null) {
            return false;
        }
        String targetId = mechanic.getId();
        for (Mechanic registeredMechanic : mechanics) {
            if (targetId.equals(registeredMechanic.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isMechanicBusy(Mechanic mechanic) {
        if (mechanic == null) {
            return false;
        }
        for (Order registeredOrder : orders) {
            if (isSameMechanic(registeredOrder.getMechanic(), mechanic)
                    && OrderStatus.IN_PROGRESS.equals(registeredOrder.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOrderWithId(String orderId) {
        for (Order registeredOrder : orders) {
            if (orderId.equals(registeredOrder.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnotherActiveOrderFor(Order order) {
        Mechanic mechanic = order.getMechanic();
        if (mechanic == null) {
            return false;
        }
        String currentId = order.getId();
        for (Order registeredOrder : orders) {
            if (currentId.equals(registeredOrder.getId())) {
                continue;
            }
            if (isSameMechanic(registeredOrder.getMechanic(), mechanic)
                    && OrderStatus.IN_PROGRESS.equals(registeredOrder.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private void replaceOrder(Order oldOrder, Order newOrder) {
        int index = this.orders.indexOf(oldOrder);
        if (index != -1) {
            this.orders.set(index, newOrder);
        }
    }

    private Order requireRegisteredOrder(Order order) {
        ensureNotNull(order, "Order cannot be null.");
        String orderId = order.getId();
        for (Order registeredOrder : orders) {
            if (orderId.equals(registeredOrder.getId())) {
                return registeredOrder;
            }
        }
        throw new IllegalStateException("Order must be registered in this service storage.");
    }

    private Car requireRegisteredCar(Car car) {
        ensureNotNull(car, "Car cannot be null.");
        for (Car registeredCar : cars) {
            if (registeredCar.getVin().equalsIgnoreCase(car.getVin())) {
                return registeredCar;
            }
        }
        throw new IllegalStateException("Car must be registered before creating an order.");
    }

    private Mechanic requireRegisteredMechanic(Mechanic mechanic) {
        ensureNotNull(mechanic, "Mechanic cannot be null.");
        for (Mechanic registeredMechanic : mechanics) {
            if (isSameMechanic(registeredMechanic, mechanic)) {
                return registeredMechanic;
            }
        }
        throw new IllegalStateException("Mechanic must be registered in this service storage.");
    }

    private void ensureNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void ensureOrderModifiable(Order order) {
        OrderStatus status = order.getStatus();
        if (OrderStatus.COMPLETED.equals(status)) {
            throw new IllegalStateException("Order is completed and cannot be modified.");
        }
        if (OrderStatus.CANCELLED.equals(status)) {
            throw new IllegalStateException("Order is cancelled and cannot be modified.");
        }
    }

    private void ensureOrderStatus(Order order, OrderStatus expectedStatus, String errorMessage) {
        if (!expectedStatus.equals(order.getStatus())) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private Order updateOrderStatus(Order current, OrderStatus newStatus) {
        Order updated = new Order(current.getId(), current.getCar(), current.getWorks(), current.getMechanic(), newStatus);
        replaceOrder(current, updated);
        return updated;
    }

    private boolean isSameMechanic(Mechanic m1, Mechanic m2) {
        if (m1 == null || m2 == null) {
            return false;
        }
        return m1.getId().equals(m2.getId());
    }
}