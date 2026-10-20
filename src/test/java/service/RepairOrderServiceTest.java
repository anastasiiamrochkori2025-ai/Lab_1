package service;

import domain.Car;
import domain.Client;
import domain.Mechanic;
import domain.MechanicSpecialization;
import domain.OrderStatus;
import domain.RepairOrder;
import domain.WorkItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pricing.PriceBreakdown;
import pricing.PricingPolicy;
import repository.RepairOrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RepairOrderService Unit Tests with Mockito")
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository repository;

    @Mock
    private PricingPolicy pricingPolicy;

    @InjectMocks
    private RepairOrderService service;

    private RepairOrder sampleOrder;

    @BeforeEach
    void setUp() {
        Client client = new Client("C-1", "Taras", "+380501234567");
        Car car = new Car("1HGCR2F83HA123456", "Skoda", "Octavia", 2019, client);
        sampleOrder = new RepairOrder("ORD-001", car);
    }

    @Test
    @DisplayName("Finding an order by an existing ID returns the corresponding entity")
    void getOrderById_ExistingId_ReturnsOrder() {
        when(repository.findById("ORD-001")).thenReturn(Optional.of(sampleOrder));

        RepairOrder actualOrder = service.getOrderById("ORD-001");

        assertNotNull(actualOrder);
        assertEquals("ORD-001", actualOrder.getId());
        verify(repository, times(1)).findById("ORD-001");
    }

    @Test
    @DisplayName("Finding an order by a non-existent ID throws NoSuchElementException")
    void getOrderById_MissingId_ThrowsNoSuchElementException() {
        when(repository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getOrderById("NON-EXISTENT"));
        verify(repository, times(1)).findById("NON-EXISTENT");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("getOrderById with null or blank ID throws IllegalArgumentException")
    void getOrderById_BlankOrNullId_ThrowsIllegalArgumentException(String invalidId) {
        assertThrows(IllegalArgumentException.class, () -> service.getOrderById(invalidId));
    }

    @Test
    @DisplayName("diagnose successfully updates order status and persists changes to repository")
    void diagnose_ValidOrder_TransitionsStatusAndSaves() {
        when(repository.findById("ORD-001")).thenReturn(Optional.of(sampleOrder));
        when(repository.save(sampleOrder)).thenReturn(sampleOrder);

        RepairOrder result = service.diagnose("ORD-001");

        assertEquals(OrderStatus.DIAGNOSED, result.getStatus());
        verify(repository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("addWork adds a work item to order and persists changes to repository")
    void addWork_ValidOrder_AddsWorkAndSaves() {
        sampleOrder.diagnose();
        WorkItem work = new WorkItem("Diagnostics", new BigDecimal("300.00"));

        when(repository.findById("ORD-001")).thenReturn(Optional.of(sampleOrder));
        when(repository.save(sampleOrder)).thenReturn(sampleOrder);

        RepairOrder result = service.addWork("ORD-001", work);

        assertEquals(1, result.getWorks().size());
        verify(repository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("cancel cancels order and persists changes to repository")
    void cancel_ValidOrder_SetsCancelledAndSaves() {
        when(repository.findById("ORD-001")).thenReturn(Optional.of(sampleOrder));
        when(repository.save(sampleOrder)).thenReturn(sampleOrder);

        RepairOrder result = service.cancel("ORD-001");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(repository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("checkout marks order as COMPLETED, persists updates, and returns checkout result")
    void checkout_ExistingOrder_CompletesOrderAndReturnsResult() {
        sampleOrder.diagnose();
        WorkItem work = new WorkItem("Diagnostics", new BigDecimal("500.00"), MechanicSpecialization.GENERAL);
        sampleOrder.addWork(work);
        sampleOrder.approve();

        Mechanic mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);
        sampleOrder.assignMechanic(mechanic);
        sampleOrder.startProgress();
        sampleOrder.completeWork(work);

        PriceBreakdown breakdown = new PriceBreakdown(
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                new BigDecimal("500.00"),
                new BigDecimal("500.00"),
                false
        );

        when(repository.findById("ORD-001")).thenReturn(Optional.of(sampleOrder));
        when(pricingPolicy.calculate(sampleOrder)).thenReturn(breakdown);
        when(repository.save(sampleOrder)).thenReturn(sampleOrder);

        OrderCheckoutResult result = service.checkout("ORD-001");

        assertNotNull(result);
        assertEquals(OrderStatus.COMPLETED, result.getOrder().getStatus());
        assertEquals(breakdown, result.getPrice());
        verify(repository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("calculatePrice delegates calculation to PricingPolicy")
    void calculatePrice_ExistingOrder_CallsPricingPolicy() {
        PriceBreakdown breakdown = new PriceBreakdown(
                new BigDecimal("1200.00"),
                BigDecimal.ZERO,
                new BigDecimal("1200.00"),
                new BigDecimal("1200.00"),
                false
        );

        when(repository.findById("ORD-001")).thenReturn(Optional.of(sampleOrder));
        when(pricingPolicy.calculate(sampleOrder)).thenReturn(breakdown);

        PriceBreakdown actualBreakdown = service.calculatePrice("ORD-001");

        assertNotNull(actualBreakdown);
        assertEquals(new BigDecimal("1200.00"), actualBreakdown.getFinalPrice());
        verify(pricingPolicy, times(1)).calculate(sampleOrder);
    }

    @Test
    @DisplayName("Retrieving orders by client ID delegates to repository")
    void getOrdersByClientId_ValidClientId_ReturnsList() {
        String clientId = "CLIENT-77";
        List<RepairOrder> expectedList = List.of(sampleOrder);
        when(repository.findAllByClientId(clientId)).thenReturn(expectedList);

        List<RepairOrder> actualList = service.getOrdersByClientId(clientId);

        assertNotNull(actualList);
        assertEquals(1, actualList.size());
        assertEquals(sampleOrder, actualList.get(0));
        verify(repository, times(1)).findAllByClientId(clientId);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("getOrdersByClientId with null or blank ID throws IllegalArgumentException")
    void getOrdersByClientId_BlankOrNullId_ThrowsIllegalArgumentException(String invalidId) {
        assertThrows(IllegalArgumentException.class, () -> service.getOrdersByClientId(invalidId));
    }
}