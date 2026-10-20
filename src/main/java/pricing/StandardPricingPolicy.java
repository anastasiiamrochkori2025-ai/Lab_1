package pricing;

import domain.RepairOrder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StandardPricingPolicy implements PricingPolicy {
    private static final BigDecimal DEFAULT_MINIMUM_ORDER_PRICE = new BigDecimal("500.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Getter
    private final BigDecimal minimumOrderPrice;

    public StandardPricingPolicy() {
        this(DEFAULT_MINIMUM_ORDER_PRICE);
    }

    public StandardPricingPolicy(BigDecimal minimumOrderPrice) {
        if (minimumOrderPrice == null || minimumOrderPrice.signum() < 0) {
            throw new IllegalArgumentException("Minimum order price cannot be null or negative.");
        }
        this.minimumOrderPrice = minimumOrderPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PriceBreakdown calculate(RepairOrder order) {
        BigDecimal basePrice = order.getTotalPrice();
        BigDecimal discountPercent = order.getClient().getDiscountPercentage();

        BigDecimal discountAmount = basePrice.multiply(discountPercent)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal priceAfterDiscount = basePrice.subtract(discountAmount);

        boolean minimumApplied = priceAfterDiscount.compareTo(minimumOrderPrice) < 0;
        BigDecimal finalPrice = minimumApplied ? minimumOrderPrice : priceAfterDiscount;

        return new PriceBreakdown(basePrice, discountAmount, priceAfterDiscount, finalPrice, minimumApplied);
    }
}