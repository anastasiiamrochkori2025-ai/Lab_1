package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository repository;

    @Mock
    private PricingPolicy pricingPolicy;

    @InjectMocks
    private RepairOrderService service;

    private RepairOrder testOrder;

    @BeforeEach
    void setUp() {
        Client client = new Client("C-100", "Тарас Шевченко", "+380501112233");
        Car car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2020, client);
        testOrder = new RepairOrder("RO-100", car, client);
    }

    @Test
    @DisplayName("Успішний сценарій оформлення та розрахунку замовлення (checkout)")
    void shouldSuccessfullyCheckoutOrder() {
        String orderId = "RO-100";
        BigDecimal expectedFinalPrice = new BigDecimal("1500.00");

        when(repository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(pricingPolicy.calculateFinalPrice(testOrder)).thenReturn(expectedFinalPrice);
        when(repository.save(any(RepairOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderCheckoutResult result = service.checkoutOrder(orderId);

        assertNotNull(result);
        assertEquals(expectedFinalPrice, result.getFinalPrice());
        assertEquals(OrderStatus.COMPLETED, result.getOrder().getStatus());

        verify(repository).findById(orderId);
        verify(pricingPolicy).calculateFinalPrice(testOrder);
        verify(repository).save(testOrder);
    }

    @Test
    @DisplayName("Викидання EntityNotFoundException, коли замовлення не знайдено в репозиторії")
    void shouldThrowEntityNotFoundExceptionWhenOrderNotFound() {
        String missingOrderId = "RO-MISSING";
        when(repository.findById(missingOrderId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> service.checkoutOrder(missingOrderId)
        );

        assertTrue(exception.getMessage().contains(missingOrderId));

        verify(repository).findById(missingOrderId);
        verifyNoInteractions(pricingPolicy);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Розрахунок вартості без зміни статусу та збереження")
    void shouldCalculateFinalPriceWithoutModifyingOrder() {
        String orderId = "RO-100";
        BigDecimal expectedPrice = new BigDecimal("850.00");

        when(repository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(pricingPolicy.calculateFinalPrice(testOrder)).thenReturn(expectedPrice);

        BigDecimal actualPrice = service.calculateFinalPrice(orderId);

        assertEquals(expectedPrice, actualPrice);
        assertEquals(OrderStatus.CREATED, testOrder.getStatus());

        verify(repository).findById(orderId);
        verify(pricingPolicy).calculateFinalPrice(testOrder);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Оновлення статусу замовлення через сервіс")
    void shouldUpdateOrderStatusSuccessfully() {
        String orderId = "RO-100";
        when(repository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(RepairOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepairOrder updated = service.updateOrderStatus(orderId, OrderStatus.IN_PROGRESS);

        assertEquals(OrderStatus.IN_PROGRESS, updated.getStatus());
        verify(repository).findById(orderId);
        verify(repository).save(testOrder);
    }
}
