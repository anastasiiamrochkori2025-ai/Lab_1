package service;

import domain.RepairOrder;
import pricing.PriceBreakdown;

import java.util.Objects;

public final class OrderCheckoutResult {
    private RepairOrder order;
    private PriceBreakdown price;

    public OrderCheckoutResult(RepairOrder order, PriceBreakdown price) {
        this.order = Objects.requireNonNull(order, "Repair order cannot be null.");
        this.price = Objects.requireNonNull(price, "Price breakdown cannot be null.");
    }

    public RepairOrder getOrder() {
        return order;
    }

    public PriceBreakdown getPrice() {
        return price;
    }
}
