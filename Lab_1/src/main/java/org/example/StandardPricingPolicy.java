package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StandardPricingPolicy implements PricingPolicy {
    private static final BigDecimal DEFAULT_MINIMUM_ORDER_PRICE = new BigDecimal("300.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private BigDecimal minimumOrderPrice;

    public StandardPricingPolicy() {
        this(DEFAULT_MINIMUM_ORDER_PRICE);
    }

    public StandardPricingPolicy(BigDecimal minimumOrderPrice) {
        if (minimumOrderPrice == null || minimumOrderPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum order price cannot be null or negative.");
        }
        this.minimumOrderPrice = minimumOrderPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateFinalPrice(RepairOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Repair order cannot be null.");
        }

        BigDecimal basePrice = order.calculateBasePrice();
        BigDecimal discountPercent = BigDecimal.ZERO;

        Client client = order.getClient();
        if (client != null && client.getDiscountPercentage() != null) {
            discountPercent = client.getDiscountPercentage();
        }

        BigDecimal discountMultiplier = discountPercent.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal discountAmount = basePrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedPrice = basePrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = discountedPrice.max(minimumOrderPrice);
        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMinimumOrderPrice() {
        return minimumOrderPrice;
    }
}
