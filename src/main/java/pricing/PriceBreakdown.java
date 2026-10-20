package pricing;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class PriceBreakdown {
    BigDecimal basePrice;
    BigDecimal discountAmount;
    BigDecimal priceAfterDiscount;
    BigDecimal finalPrice;
    boolean minimumApplied;

    public BigDecimal getMinimumAdjustment() {
        return finalPrice.subtract(priceAfterDiscount);
    }
}