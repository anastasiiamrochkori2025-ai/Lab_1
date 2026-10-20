package pricing;

import domain.Car;
import domain.Client;
import domain.MechanicSpecialization;
import domain.RepairOrder;
import domain.SparePart;
import domain.WorkItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StandardPricingPolicy Pricing Strategy Tests")
class StandardPricingPolicyTest {

    private StandardPricingPolicy pricingPolicy;
    private final BigDecimal MIN_PRICE = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        pricingPolicy = new StandardPricingPolicy(MIN_PRICE);
    }

    @ParameterizedTest(name = "Labor cost: {0}, Discount: {1}%, Final payable: {2}")
    @CsvSource({
            "1000.00,  0.00, 1000.00",
            "1000.00, 10.00,  900.00",
            "1000.00, 25.00,  750.00",
            "200.00,  10.00,  500.00",
            "100.00,  50.00,  500.00"
    })
    @DisplayName("Calculates final price taking into account client discount and minimum order threshold")
    void calculate_VariousInputs_ReturnsExpectedFinalPrice(String laborCost, String discount, String expectedFinal) {
        Client client = new Client("C-01", "Oleksandr", "+380501112233", new BigDecimal(discount));
        Car car = new Car("1HGCR2F83HA123456", "Honda", "Civic", 2020, client);
        RepairOrder order = new RepairOrder("RO-TEST", car);
        order.diagnose();
        order.addWork(new WorkItem("Maintenance", new BigDecimal(laborCost), MechanicSpecialization.GENERAL));

        PriceBreakdown breakdown = pricingPolicy.calculate(order);

        assertEquals(new BigDecimal(expectedFinal), breakdown.getFinalPrice());
    }

    @Test
    @DisplayName("Validates full PriceBreakdown details: base price, discount amount, and final total")
    void calculate_FullOrder_ReturnsAccurateBreakdown() {
        Client client = new Client("C-02", "Maryna", "+380671112233", new BigDecimal("10.00"));
        Car car = new Car("1HGCR2F83HA654321", "Toyota", "RAV4", 2021, client);
        RepairOrder order = new RepairOrder("RO-BRK", car);
        order.diagnose();

        List<SparePart> parts = List.of(new SparePart("SP-1", "Brake Pads", "Brembo", new BigDecimal("1200.00")));
        WorkItem work = new WorkItem("Parts Replacement", new BigDecimal("800.00"), MechanicSpecialization.GENERAL, parts);
        order.addWork(work);

        PriceBreakdown breakdown = pricingPolicy.calculate(order);

        assertNotNull(breakdown);
        assertEquals(new BigDecimal("2000.00"), breakdown.getBasePrice());
        assertEquals(new BigDecimal("200.00"), breakdown.getDiscountAmount());
        assertEquals(new BigDecimal("1800.00"), breakdown.getPriceAfterDiscount());
        assertEquals(new BigDecimal("1800.00"), breakdown.getFinalPrice());
        assertFalse(breakdown.isMinimumApplied());
    }

    @Test
    @DisplayName("Attempting to initialize pricing policy with a negative minimum price throws IllegalArgumentException")
    void constructor_NegativeMinimumPrice_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StandardPricingPolicy(new BigDecimal("-50.00")));
    }
}