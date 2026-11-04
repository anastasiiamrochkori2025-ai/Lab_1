package service;

import domain.Mechanic;
import domain.RepairOrder;
import domain.WorkItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pricing.PriceBreakdown;
import pricing.PricingPolicy;
import repository.RepairOrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@Transactional
public class RepairOrderService {

    private final RepairOrderRepository repository;
    private final PricingPolicy pricingPolicy;

    @Autowired
    public RepairOrderService(RepairOrderRepository repository, PricingPolicy pricingPolicy) {
        this.repository = Objects.requireNonNull(repository, "Repair order repository cannot be null.");
        this.pricingPolicy = Objects.requireNonNull(pricingPolicy, "Pricing policy cannot be null.");
    }

    public RepairOrder saveOrder(RepairOrder order) {
        return repository.save(Objects.requireNonNull(order, "Repair order cannot be null."));
    }

    @Transactional(readOnly = true)
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

    @Transactional(rollbackFor = Exception.class)
    public RepairOrder addWorks(String orderId, List<WorkItem> works) {
        if (works == null) {
            throw new IllegalArgumentException("Works batch cannot be null.");
        }
        RepairOrder order = getOrderById(orderId);
        List<WorkItem> validatedWorks = works.stream()
                .map(work -> {
                    if (work == null) {
                        throw new IllegalArgumentException("Work item in batch cannot be null.");
                    }
                    return work;
                })
                .toList();
        validatedWorks.forEach(order::addWork);
        return repository.save(order);
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

    @Transactional(readOnly = true)
    public PriceBreakdown calculatePrice(String orderId) {
        return pricingPolicy.calculate(getOrderById(orderId));
    }

    @Transactional
    public RepairOrder completeOrder(String orderId) {
        RepairOrder order = getOrderById(orderId);
        PriceBreakdown price = pricingPolicy.calculate(order);
        order.complete(price.getFinalPrice(), LocalDateTime.now());
        return repository.save(order);
    }

    @Transactional
    public OrderCheckoutResult checkout(String orderId) {
        RepairOrder order = completeOrder(orderId);
        PriceBreakdown price = pricingPolicy.calculate(order);
        return new OrderCheckoutResult(order, price);
    }

    @Transactional(readOnly = true)
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
