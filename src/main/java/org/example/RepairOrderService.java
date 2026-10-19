package org.example;

import java.math.BigDecimal;
import java.util.List;

public class RepairOrderService {
    private RepairOrderRepository repository;
    private PricingPolicy pricingPolicy;

    public RepairOrderService(RepairOrderRepository repository, PricingPolicy pricingPolicy) {
        if (repository == null) {
            throw new IllegalArgumentException("RepairOrderRepository cannot be null.");
        }
        if (pricingPolicy == null) {
            throw new IllegalArgumentException("PricingPolicy cannot be null.");
        }
        this.repository = repository;
        this.pricingPolicy = pricingPolicy;
    }

    public OrderCheckoutResult checkoutOrder(String orderId) {
        return checkoutOrder(orderId, OrderStatus.COMPLETED);
    }

    public OrderCheckoutResult checkoutOrder(String orderId, OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("Target order status cannot be null.");
        }

        // 1. Отримання замовлення за id (або викидання EntityNotFoundException)
        RepairOrder order = getOrderById(orderId);

        // 2. Розрахунок підсумкової вартості через PricingPolicy
        BigDecimal finalPrice = pricingPolicy.calculateFinalPrice(order);

        // 3. Зміна статусу замовлення та збереження оновленого стану в репозиторій
        order.setStatus(targetStatus);
        RepairOrder savedOrder = repository.save(order);

        return new OrderCheckoutResult(savedOrder, finalPrice);
    }

    public BigDecimal calculateFinalPrice(String orderId) {
        RepairOrder order = getOrderById(orderId);
        return pricingPolicy.calculateFinalPrice(order);
    }

    public RepairOrder getOrderById(String orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Repair order with id '" + orderId + "' was not found."));
    }

    public RepairOrder saveOrder(RepairOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Repair order cannot be null.");
        }
        return repository.save(order);
    }

    public RepairOrder updateOrderStatus(String orderId, OrderStatus newStatus) {
        RepairOrder order = getOrderById(orderId);
        order.setStatus(newStatus);
        return repository.save(order);
    }

    public List<RepairOrder> getOrdersByClientId(String clientId) {
        return repository.findAllByClientId(clientId);
    }
}
