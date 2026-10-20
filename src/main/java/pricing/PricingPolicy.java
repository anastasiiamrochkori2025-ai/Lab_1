package pricing;

import domain.RepairOrder;

public interface PricingPolicy {
    PriceBreakdown calculate(RepairOrder order);
}
