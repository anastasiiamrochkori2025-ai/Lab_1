package org.example;

import java.math.BigDecimal;

public record OrderCheckoutResult(RepairOrder order, BigDecimal finalPrice) {
    public RepairOrder getOrder() {
        return order;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
}
