package service;

import domain.Mechanic;
import domain.RepairOrder;
import domain.WorkItem;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pricing.PriceBreakdown;
import pricing.PricingPolicy;
import repository.RepairOrderRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class RepairOrderService {
    @NonNull
    private RepairOrderRepository repository;
    @NonNull
    private PricingPolicy pricingPolicy;

    public RepairOrder saveOrder(RepairOrder order) {
        return repository.save(Objects.requireNonNull(order, "Repair order cannot be null."));
    }

    public RepairOrder getOrderById(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order id cannot be null or blank.");
        }
        String id = orderId.trim();
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Repair order with id '" + id + "' was not found."));
    }

    public RepairOrder diagnose(String orderId) {
        return update(orderId, RepairOrder::diagnose);
    }

    public RepairOrder addWork(String orderId, WorkItem work) {
        return update(orderId, order -> order.addWork(work));
    }

    public RepairOrder approve(String orderId) {
        return update(orderId, RepairOrder::approve);
    }

    public RepairOrder assignMechanic(String orderId, Mechanic mechanic) {
        return update(orderId, order -> order.assignMechanic(mechanic));
    }

    public RepairOrder startProgress(String orderId) {
        return update(orderId, RepairOrder::startProgress);
    }

    public RepairOrder completeWork(String orderId, WorkItem work) {
        return update(orderId, order -> order.completeWork(work));
    }

    public RepairOrder cancel(String orderId) {
        return update(orderId, RepairOrder::cancel);
    }

    public PriceBreakdown calculatePrice(String orderId) {
        return pricingPolicy.calculate(getOrderById(orderId));
    }

    public OrderCheckoutResult checkout(String orderId) {
        RepairOrder order = getOrderById(orderId);
        PriceBreakdown price = pricingPolicy.calculate(order);
        order.complete();
        return new OrderCheckoutResult(repository.save(order), price);
    }

    public List<RepairOrder> getOrdersByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client id cannot be null or blank.");
        }
        return repository.findAllByClientId(clientId.trim());
    }

    private RepairOrder update(String orderId, Consumer<RepairOrder> operation) {
        RepairOrder order = getOrderById(orderId);
        operation.accept(order);
        return repository.save(order);
    }
}