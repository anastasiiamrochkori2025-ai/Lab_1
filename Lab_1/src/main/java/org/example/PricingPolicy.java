package org.example;

import java.math.BigDecimal;

public interface PricingPolicy {
    BigDecimal calculateFinalPrice(RepairOrder order);
}
