package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comprehensive RepairOrder Domain Model Tests")
public class RepairOrderTest {

    private RepairOrder order;
    private Client client;
    private Car car;

    @BeforeEach
    void setUp() {
        client = new Client("C-101", "Oleksandr Melnyk", "+380501234567", new BigDecimal("10.00"));
        car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2020, client);
        order = new RepairOrder("RO-2026-001", car);
    }

    @Test
    @DisplayName("Works list should be unmodifiable from outside")
    void getWorks_AttemptToModifyList_ThrowsUnsupportedOperationException() {
        order.diagnose();
        WorkItem work = new WorkItem("Diagnostics", new BigDecimal("300.00"));
        order.addWork(work);

        List<WorkItem> works = order.getWorks();

        assertThrows(UnsupportedOperationException.class,
                () -> works.add(new WorkItem("Another work", new BigDecimal("100.00"))));
    }

    @Nested
    @DisplayName("1. Diagnostics Transition Tests (DiagnosingTest)")
    class DiagnosingTest {
        @Test
        @DisplayName("Should successfully transition a new order from CREATED to DIAGNOSED status")
        void diagnose_FromCreatedStatus_SetsDiagnosedStatus() {
            assertEquals(OrderStatus.CREATED, order.getStatus());

            order.diagnose();

            assertEquals(OrderStatus.DIAGNOSED, order.getStatus());
        }

        @Test
        @DisplayName("Attempting to re-diagnose should throw IllegalStateException")
        void diagnose_AlreadyDiagnosed_ThrowsIllegalStateException() {
            order.diagnose();

            assertThrows(IllegalStateException.class, () -> order.diagnose());
        }
    }

    @Nested
    @DisplayName("2. Order Approval Tests (ApprovingTest)")
    class ApprovingTest {
        @Test
        @DisplayName("Should successfully approve a diagnosed order that contains work items")
        void approve_DiagnosedOrderWithWorks_SetsApprovedStatus() {
            order.diagnose();
            order.addWork(new WorkItem("Diagnostics", new BigDecimal("300.00")));

            order.approve();

            assertEquals(OrderStatus.APPROVED, order.getStatus());
        }

        @Test
        @DisplayName("Should disallow approving an order without any work items")
        void approve_NoWorks_ThrowsIllegalStateException() {
            order.diagnose();

            assertThrows(IllegalStateException.class, () -> order.approve());
        }

        @Test
        @DisplayName("Should disallow approving an order prior to diagnosis")
        void approve_BeforeDiagnosis_ThrowsIllegalStateException() {
            assertThrows(IllegalStateException.class, () -> order.approve());
        }
    }

    @Nested
    @DisplayName("3. Mechanic Assignment Tests (AssignMechanicTest)")
    class AssignMechanicTest {
        private Mechanic generalMechanic;

        @BeforeEach
        void initOrder() {
            order.diagnose();
            order.addWork(new WorkItem("Oil Change", new BigDecimal("400.00"), MechanicSpecialization.GENERAL));
            order.approve();
            generalMechanic = new Mechanic("M-1", "Ivan Koval", MechanicSpecialization.GENERAL);
        }

        @Test
        @DisplayName("Successfully assigning an available mechanic marks them as busy")
        void assignMechanic_AvailableMechanic_AssignsAndMakesBusy() {
            assertTrue(generalMechanic.isAvailable());

            order.assignMechanic(generalMechanic);

            assertEquals(generalMechanic, order.getMechanic());
            assertFalse(generalMechanic.isAvailable());
        }

        @Test
        @DisplayName("Assigning an already busy mechanic should throw IllegalStateException")
        void assignMechanic_AlreadyBusyMechanic_ThrowsIllegalStateException() {
            generalMechanic.makeBusy();

            assertThrows(IllegalStateException.class, () -> order.assignMechanic(generalMechanic));
        }

        @Test
        @DisplayName("Assigning a mechanic with an incompatible specialization should throw IllegalStateException")
        void assignMechanic_IncompatibleSpecialization_ThrowsIllegalStateException() {
            RepairOrder electricalOrder = new RepairOrder("RO-ELEC", car);
            electricalOrder.diagnose();
            electricalOrder.addWork(new WorkItem("Wiring Repair", new BigDecimal("500.00"), MechanicSpecialization.ELECTRICAL));
            electricalOrder.approve();

            Mechanic engineMechanic = new Mechanic("M-2", "Petro", MechanicSpecialization.ENGINE);

            assertThrows(IllegalStateException.class, () -> electricalOrder.assignMechanic(engineMechanic));
        }
    }

    @Nested
    @DisplayName("4. In-Progress Transition Tests (MakeInProgressTest)")
    class MakeInProgressTest {
        @Test
        @DisplayName("Should successfully start work once approved and assigned to a mechanic")
        void startProgress_ApprovedWithMechanic_SetsInProgressStatus() {
            order.diagnose();
            order.addWork(new WorkItem("Repair", new BigDecimal("500.00")));
            order.approve();
            Mechanic mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);
            order.assignMechanic(mechanic);

            order.startProgress();

            assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        }

        @Test
        @DisplayName("Should disallow starting progress without an assigned mechanic")
        void startProgress_WithoutMechanic_ThrowsIllegalStateException() {
            order.diagnose();
            order.addWork(new WorkItem("Repair", new BigDecimal("500.00")));
            order.approve();

            assertThrows(IllegalStateException.class, () -> order.startProgress());
        }
    }

    @Nested
    @DisplayName("5. Order Completion Tests (CompleteTest)")
    class CompleteTest {
        private WorkItem work;
        private Mechanic mechanic;

        @BeforeEach
        void prepareInProgressOrder() {
            order.diagnose();
            work = new WorkItem("Diagnostics", new BigDecimal("500.00"));
            order.addWork(work);
            order.approve();
            mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);
            order.assignMechanic(mechanic);
            order.startProgress();
        }

        @Test
        @DisplayName("Completing an order with finished works sets COMPLETED status and frees the mechanic")
        void complete_AllWorksCompleted_SetsCompletedStatusAndFreesMechanic() {
            order.completeWork(work);
            assertTrue(work.isCompleted());

            order.complete();

            assertEquals(OrderStatus.COMPLETED, order.getStatus());
            assertTrue(mechanic.isAvailable());
        }

        @Test
        @DisplayName("Should disallow completing an order that has unfinished works")
        void complete_UnfinishedWorks_ThrowsIllegalStateException() {
            assertFalse(work.isCompleted());

            assertThrows(IllegalStateException.class, () -> order.complete());
        }
    }

    @Nested
    @DisplayName("6. Order Cancellation Tests (CancelTest)")
    class CancelTest {
        @Test
        @DisplayName("Cancelling an assigned order transitions it to CANCELLED and frees the assigned mechanic")
        void cancel_AssignedOrder_SetsCancelledAndFreesMechanic() {
            order.diagnose();
            order.addWork(new WorkItem("Repair", new BigDecimal("300.00")));
            order.approve();
            Mechanic mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);
            order.assignMechanic(mechanic);
            assertFalse(mechanic.isAvailable());

            order.cancel();

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            assertTrue(mechanic.isAvailable());
            assertNull(order.getMechanic());
        }

        @Test
        @DisplayName("Should disallow cancelling an already completed order")
        void cancel_CompletedOrder_ThrowsIllegalStateException() {
            order.diagnose();
            WorkItem work = new WorkItem("Repair", new BigDecimal("300.00"));
            order.addWork(work);
            order.approve();
            Mechanic mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);
            order.assignMechanic(mechanic);
            order.startProgress();
            order.completeWork(work);
            order.complete();

            assertThrows(IllegalStateException.class, () -> order.cancel());
        }
    }

    @Nested
    @DisplayName("7. Labor Cost Calculation Tests (GetWorkCostTest)")
    class GetWorkCostTest {
        @Test
        @DisplayName("Calculates total labor cost for all added works")
        void getLaborTotal_MultipleWorks_ReturnsSumOfLaborCosts() {
            order.diagnose();
            order.addWork(new WorkItem("Labor 1", new BigDecimal("450.50")));
            order.addWork(new WorkItem("Labor 2", new BigDecimal("350.50")));

            BigDecimal totalLabor = order.getLaborTotal();

            assertEquals(new BigDecimal("801.00"), totalLabor);
        }
    }

    @Nested
    @DisplayName("8. Spare Parts Cost Calculation Tests (GetPartsPriceTest)")
    class GetPartsPriceTest {
        @Test
        @DisplayName("Calculates total spare parts cost across all works in the order")
        void getPartsTotal_WorksWithParts_ReturnsSumOfPartsPrices() {
            order.diagnose();
            List<SparePart> parts1 = List.of(new SparePart("P1", "Filter", new BigDecimal("300.00")));
            List<SparePart> parts2 = List.of(
                    new SparePart("P2", "Engine Oil", new BigDecimal("1200.00")),
                    new SparePart("P3", "Gasket", new BigDecimal("150.00"))
            );

            order.addWork(new WorkItem("Labor 1", new BigDecimal("200.00"), MechanicSpecialization.GENERAL, parts1));
            order.addWork(new WorkItem("Labor 2", new BigDecimal("400.00"), MechanicSpecialization.GENERAL, parts2));

            BigDecimal totalParts = order.getPartsTotal();

            assertEquals(new BigDecimal("1650.00"), totalParts);
        }
    }

    @Nested
    @DisplayName("9. Total Order Price Calculation Tests (GetTotalOrderPriceTest)")
    class GetTotalOrderPriceTest {
        @Test
        @DisplayName("Calculates combined total order price (labor + spare parts)")
        void getTotalPrice_WorksAndParts_ReturnsExactCombinedTotal() {
            order.diagnose();
            List<SparePart> parts = List.of(new SparePart("P1", "Brake Pads", new BigDecimal("1850.00")));
            WorkItem work = new WorkItem("Brake Pad Replacement", new BigDecimal("600.00"), MechanicSpecialization.GENERAL, parts);
            order.addWork(work);

            BigDecimal totalPrice = order.getTotalPrice();

            assertEquals(new BigDecimal("2450.00"), totalPrice);
        }
    }
}