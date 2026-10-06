package org.example;

import java.math.BigDecimal;
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
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null.");
        }
        boolean registered = hasRegisteredCar(car);
        if (registered) {
            String vin = car.getVin();
            throw new IllegalArgumentException("Car with VIN " + vin + " is already registered.");
        }
        cars.add(car);
    }

    public void registerMechanic(Mechanic mechanic) {
        if (mechanic == null) {
            throw new IllegalArgumentException("Mechanic cannot be null.");
        }
        boolean registered = hasRegisteredMechanic(mechanic);
        if (registered) {
            String id = mechanic.getId();
            throw new IllegalArgumentException("Mechanic with id " + id + " is already registered.");
        }
        mechanics.add(mechanic);
    }

    public Order createOrder(String orderId, Car car) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null.");
        }
        boolean isBlankId = orderId.isBlank();
        if (isBlankId) {
            throw new IllegalArgumentException("Order id cannot be blank.");
        }
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null.");
        }
        boolean carRegistered = hasRegisteredCar(car);
        if (!carRegistered) {
            throw new IllegalStateException("Car must be registered before creating an order.");
        }
        boolean orderExists = hasOrderWithId(orderId);
        if (orderExists) {
            throw new IllegalArgumentException("Order with id " + orderId + " already exists.");
        }
        Order order = new Order(orderId, car);
        orders.add(order);
        return order;
    }

    public Order addWorkToOrder(Order order, WorkItem work) {
        Order current = requireRegisteredOrder(order);
        if (work == null) {
            throw new IllegalArgumentException("Work item cannot be null.");
        }
        OrderStatus status = current.getStatus();
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order is completed and cannot be modified.");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is cancelled and cannot be modified.");
        }
        List<WorkItem> currentWorks = current.getWorks();
        List<WorkItem> newWorks = new ArrayList<>(currentWorks);
        newWorks.add(work);
        String orderId = current.getId();
        Car car = current.getCar();
        Mechanic mechanic = current.getMechanic();
        Order updated = new Order(orderId, car, newWorks, mechanic, status);
        replaceOrder(current, updated);
        return updated;
    }

    public Order diagnoseOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        OrderStatus status = current.getStatus();
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only a created order can be diagnosed.");
        }
        String orderId = current.getId();
        Car car = current.getCar();
        List<WorkItem> works = current.getWorks();
        Mechanic mechanic = current.getMechanic();
        Order updated = new Order(orderId, car, works, mechanic, OrderStatus.DIAGNOSED);
        replaceOrder(current, updated);
        return updated;
    }

    public Order approveOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        OrderStatus status = current.getStatus();
        if (status != OrderStatus.DIAGNOSED) {
            throw new IllegalStateException("Only a diagnosed order can be approved.");
        }
        List<WorkItem> works = current.getWorks();
        boolean empty = works.isEmpty();
        if (empty) {
            throw new IllegalStateException("Order cannot be approved without work items.");
        }
        String orderId = current.getId();
        Car car = current.getCar();
        Mechanic mechanic = current.getMechanic();
        Order updated = new Order(orderId, car, works, mechanic, OrderStatus.APPROVED);
        replaceOrder(current, updated);
        return updated;
    }

    public Order assignMechanicToOrder(Order order, Mechanic mechanic) {
        Order current = requireRegisteredOrder(order);
        Mechanic registeredMechanic = requireRegisteredMechanic(mechanic);
        OrderStatus status = current.getStatus();
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order is completed and cannot be modified.");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is cancelled and cannot be modified.");
        }
        if (status == OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Order is in progress, mechanic reassign is forbidden.");
        }
        boolean busy = isMechanicBusy(registeredMechanic);
        if (busy) {
            throw new IllegalStateException("Cannot assign a busy mechanic.");
        }
        String orderId = current.getId();
        Car car = current.getCar();
        List<WorkItem> works = current.getWorks();
        Order updated = new Order(orderId, car, works, registeredMechanic, status);
        replaceOrder(current, updated);
        return updated;
    }

    public Order startOrderProgress(Order order) {
        Order current = requireRegisteredOrder(order);
        OrderStatus status = current.getStatus();
        if (status != OrderStatus.APPROVED) {
            throw new IllegalStateException("Only an approved order can be started.");
        }
        Mechanic mechanic = current.getMechanic();
        if (mechanic == null) {
            throw new IllegalStateException("Order cannot be started without an assigned mechanic.");
        }
        boolean anotherActive = hasAnotherActiveOrderFor(current);
        if (anotherActive) {
            throw new IllegalStateException("Mechanic cannot have two active orders at the same time.");
        }
        String orderId = current.getId();
        Car car = current.getCar();
        List<WorkItem> works = current.getWorks();
        Order updated = new Order(orderId, car, works, mechanic, OrderStatus.IN_PROGRESS);
        replaceOrder(current, updated);
        return updated;
    }

    public WorkItem completeWork(WorkItem work) {
        if (work == null) {
            throw new IllegalArgumentException("Work item cannot be null.");
        }
        WorkStatus currentStatus = work.getStatus();
        if (currentStatus == WorkStatus.COMPLETED) {
            throw new IllegalStateException("Work item is already completed.");
        }
        String desc = work.getDescription();
        BigDecimal parts = work.getPartsCost();
        BigDecimal labor = work.getLaborCost();
        WorkItem completedWork = new WorkItem(desc, parts, labor, WorkStatus.COMPLETED);

        for (Order registeredOrder : orders) {
            List<WorkItem> works = registeredOrder.getWorks();
            boolean contains = false;
            for (WorkItem item : works) {
                String itemDesc = item.getDescription();
                boolean match = itemDesc.equals(desc);
                if (match) {
                    contains = true;
                    break;
                }
            }
            if (contains) {
                List<WorkItem> newWorks = new ArrayList<>();
                for (WorkItem item : works) {
                    String itemDesc = item.getDescription();
                    boolean match = itemDesc.equals(desc);
                    if (match) {
                        newWorks.add(completedWork);
                    } else {
                        newWorks.add(item);
                    }
                }
                String orderId = registeredOrder.getId();
                Car car = registeredOrder.getCar();
                Mechanic mechanic = registeredOrder.getMechanic();
                OrderStatus status = registeredOrder.getStatus();
                Order updatedOrder = new Order(orderId, car, newWorks, mechanic, status);
                replaceOrder(registeredOrder, updatedOrder);
            }
        }
        return completedWork;
    }

    public Order completeOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        OrderStatus status = current.getStatus();
        if (status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only an order in progress can be completed.");
        }
        List<WorkItem> works = current.getWorks();
        for (WorkItem work : works) {
            WorkStatus workStatus = work.getStatus();
            if (workStatus != WorkStatus.COMPLETED) {
                throw new IllegalStateException("Order cannot be completed until all work items are completed.");
            }
        }
        String orderId = current.getId();
        Car car = current.getCar();
        Mechanic mechanic = current.getMechanic();
        Order updated = new Order(orderId, car, works, mechanic, OrderStatus.COMPLETED);
        replaceOrder(current, updated);
        return updated;
    }

    public Order cancelOrder(Order order) {
        Order current = requireRegisteredOrder(order);
        OrderStatus status = current.getStatus();
        boolean isCreated = status == OrderStatus.CREATED;
        boolean isDiagnosed = status == OrderStatus.DIAGNOSED;
        boolean isApproved = status == OrderStatus.APPROVED;
        if (!isCreated && !isDiagnosed && !isApproved) {
            throw new IllegalStateException("Only created, diagnosed, or approved orders can be cancelled.");
        }
        String orderId = current.getId();
        Car car = current.getCar();
        List<WorkItem> works = current.getWorks();
        Mechanic mechanic = current.getMechanic();
        Order updated = new Order(orderId, car, works, mechanic, OrderStatus.CANCELLED);
        replaceOrder(current, updated);
        return updated;
    }

    public BigDecimal calculateTotalCost(Order order) {
        Order current = requireRegisteredOrder(order);
        BigDecimal totalCost = BigDecimal.ZERO;
        List<WorkItem> works = current.getWorks();
        for (WorkItem work : works) {
            BigDecimal partsCost = work.getPartsCost();
            BigDecimal laborCost = work.getLaborCost();
            BigDecimal workCost = partsCost.add(laborCost);
            totalCost = totalCost.add(workCost);
        }
        return totalCost;
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

    public boolean hasRegisteredCar(Car car) {
        if (car == null) {
            return false;
        }
        String targetVin = car.getVin();
        for (Car registeredCar : cars) {
            String existingVin = registeredCar.getVin();
            boolean match = existingVin.equalsIgnoreCase(targetVin);
            if (match) {
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
            String existingId = registeredMechanic.getId();
            boolean match = existingId.equals(targetId);
            if (match) {
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
            Mechanic assigned = registeredOrder.getMechanic();
            if (assigned == mechanic) {
                OrderStatus status = registeredOrder.getStatus();
                if (status == OrderStatus.IN_PROGRESS) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasOrderWithId(String orderId) {
        for (Order registeredOrder : orders) {
            String existingId = registeredOrder.getId();
            boolean match = existingId.equals(orderId);
            if (match) {
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
            String existingId = registeredOrder.getId();
            boolean sameOrder = existingId.equals(currentId);
            if (sameOrder) {
                continue;
            }
            Mechanic assigned = registeredOrder.getMechanic();
            if (assigned == mechanic) {
                OrderStatus status = registeredOrder.getStatus();
                if (status == OrderStatus.IN_PROGRESS) {
                    return true;
                }
            }
        }
        return false;
    }

    private void replaceOrder(Order oldOrder, Order newOrder) {
        List<Order> updatedList = new ArrayList<>();
        for (Order order : orders) {
            if (order.getId().equals(oldOrder.getId())) {
                updatedList.add(newOrder);
            } else {
                updatedList.add(order);
            }
        }
        this.orders.clear();
        this.orders.addAll(updatedList);
    }

    private Order requireRegisteredOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        String orderId = order.getId();
        for (Order registeredOrder : orders) {
            String regId = registeredOrder.getId();
            boolean match = regId.equals(orderId);
            if (match) {
                return registeredOrder;
            }
        }
        throw new IllegalStateException("Order must be registered in this service storage.");
    }

    private Mechanic requireRegisteredMechanic(Mechanic mechanic) {
        if (mechanic == null) {
            throw new IllegalArgumentException("Mechanic cannot be null.");
        }
        for (Mechanic registeredMechanic : mechanics) {
            if (registeredMechanic == mechanic) {
                return registeredMechanic;
            }
        }
        throw new IllegalStateException("Mechanic must be registered in this service storage.");
    }
}