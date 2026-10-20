package pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriceBreakdown Class Tests")
class PriceBreakdownTest {

    @Test
    @DisplayName("Stores values correctly and computes minimum price adjustment")
    void constructor_ValidData_StoresValuesAndCalculatesAdjustment() {
        BigDecimal base = new BigDecimal("200.00");
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal afterDiscount = new BigDecimal("200.00");
        BigDecimal finalPrice = new BigDecimal("300.00");

        PriceBreakdown breakdown = new PriceBreakdown(base, discount, afterDiscount, finalPrice, true);

        assertEquals(base, breakdown.getBasePrice());
        assertEquals(discount, breakdown.getDiscountAmount());
        assertEquals(afterDiscount, breakdown.getPriceAfterDiscount());
        assertEquals(finalPrice, breakdown.getFinalPrice());
        assertTrue(breakdown.isMinimumApplied());
        assertEquals(new BigDecimal("100.00"), breakdown.getMinimumAdjustment());
    }
}