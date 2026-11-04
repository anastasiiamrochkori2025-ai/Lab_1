package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandardPricingPolicyTest {

    private PricingPolicy pricingPolicy;
    private Client client;
    private Car car;

    @BeforeEach
    void setUp() {
        pricingPolicy = new StandardPricingPolicy(new BigDecimal("300.00"));
        client = new Client("C-001", "Іван Франко", "+380501234567");
        car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2019, client);
    }

    @Test
    @DisplayName("Розрахунок вартості зі звичайною клієнтською знижкою (15%)")
    void shouldApplyClientDiscountCorrectly() {
        client.setDiscountPercentage(new BigDecimal("15.00"));
        RepairOrder order = new RepairOrder("RO-01", car, client);
        order.addWork(new WorkItem("Ремонт підвіски", new BigDecimal("600.00"), new BigDecimal("400.00")));

        BigDecimal finalPrice = pricingPolicy.calculateFinalPrice(order);

        assertEquals(new BigDecimal("850.00"), finalPrice);
    }

    @Test
    @DisplayName("Коректність округлення Half-Up (123.45 грн - 10% знижки)")
    void shouldRoundHalfUpCorrectly() {
        client.setDiscountPercentage(new BigDecimal("10.00"));
        RepairOrder order = new RepairOrder("RO-02", car, client);
        order.addWork(new WorkItem("Дрібний ремонт", new BigDecimal("73.45"), new BigDecimal("50.00")));

        PricingPolicy policy = new StandardPricingPolicy(new BigDecimal("100.00"));
        BigDecimal finalPrice = policy.calculateFinalPrice(order);

        assertEquals(new BigDecimal("111.10"), finalPrice);
    }

    @Test
    @DisplayName("Спрацьовування порогу мінімальної вартості замовлення")
    void shouldEnforceMinimumOrderPriceThreshold() {
        client.setDiscountPercentage(new BigDecimal("50.00"));
        RepairOrder order = new RepairOrder("RO-03", car, client);
        order.addWork(new WorkItem("Діагностика", BigDecimal.ZERO, new BigDecimal("200.00")));

        BigDecimal finalPrice = pricingPolicy.calculateFinalPrice(order);

        assertEquals(new BigDecimal("300.00"), finalPrice);
    }

    @Test
    @DisplayName("Розрахунок без знижки клієнта (0%)")
    void shouldReturnBasePriceWhenClientHasZeroDiscount() {
        client.setDiscountPercentage(BigDecimal.ZERO);
        RepairOrder order = new RepairOrder("RO-04", car, client);
        order.addWork(new WorkItem("Заміна масла", new BigDecimal("300.00"), new BigDecimal("200.00")));

        BigDecimal finalPrice = pricingPolicy.calculateFinalPrice(order);

        assertEquals(new BigDecimal("500.00"), finalPrice);
    }

    @Test
    @DisplayName("Викидання винятку при передачі null замовлення")
    void shouldThrowExceptionWhenOrderIsNull() {
        assertThrows(IllegalArgumentException.class, () -> pricingPolicy.calculateFinalPrice(null));
    }

    @Test
    @DisplayName("Викидання винятку при від'ємному мінімальному тарифі")
    void shouldThrowExceptionForNegativeMinimumOrderPrice() {
        assertThrows(IllegalArgumentException.class, () -> new StandardPricingPolicy(new BigDecimal("-50.00")));
    }
}
