package app;

import domain.Car;
import domain.Client;
import domain.Mechanic;
import domain.MechanicSpecialization;
import domain.OrderStatus;
import domain.RepairOrder;
import domain.SparePart;
import domain.WorkItem;
import domain.WorkStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pricing.PriceBreakdown;
import pricing.PricingPolicy;
import pricing.StandardPricingPolicy;
import repository.CarRepository;
import repository.ClientRepository;
import repository.MechanicRepository;
import repository.RepairOrderRepository;
import repository.SparePartRepository;
import repository.WorkItemRepository;
import service.OrderCheckoutResult;
import service.RepairOrderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Spring Boot, JPA, Flyway, and IoC Integration Tests")
class RepairOrderApplicationTests {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private PricingPolicy pricingPolicy;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MechanicRepository mechanicRepository;

    @Autowired
    private SparePartRepository sparePartRepository;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Spring context loads and injects beans via constructor")
    void contextLoads_BeansInjectedSuccessfully() {
        assertNotNull(repairOrderService);
        assertNotNull(pricingPolicy);
        assertNotNull(repairOrderRepository);
        assertNotNull(clientRepository);
        assertNotNull(carRepository);
        assertNotNull(mechanicRepository);
        assertNotNull(sparePartRepository);
        assertNotNull(workItemRepository);

        assertInstanceOf(StandardPricingPolicy.class, pricingPolicy);
    }

    @Test
    @DisplayName("JPA mapping, cascade persist, and Flyway schema end-to-end test")
    void fullJpaPersistenceWorkflow_SavesAndRetrievesCorrectly() {
        Client client = new Client("C-SPRING-1", "John Spring", "+380501112233", new BigDecimal("10.00"));
        clientRepository.save(client);

        Car car = new Car("1HGCR2F83HA999999", "Honda", "Civic", 2021, client);
        carRepository.save(car);

        Mechanic mechanic = new Mechanic("M-SPRING-1", "Dave Master", MechanicSpecialization.GENERAL);
        mechanicRepository.save(mechanic);

        SparePart part = new SparePart("BP-999", "Premium Brake Pads", "Brembo", new BigDecimal("1500.00"));
        sparePartRepository.save(part);

        RepairOrder order = new RepairOrder("RO-SPRING-001", car);
        repairOrderService.saveOrder(order);

        repairOrderService.diagnose("RO-SPRING-001");

        WorkItem work = new WorkItem("Brake replacement", new BigDecimal("600.00"),
                MechanicSpecialization.GENERAL, List.of(part));
        repairOrderService.addWork("RO-SPRING-001", work);

        repairOrderService.approve("RO-SPRING-001");
        repairOrderService.assignMechanic("RO-SPRING-001", mechanic);
        repairOrderService.startProgress("RO-SPRING-001");
        repairOrderService.completeWork("RO-SPRING-001", work);

        OrderCheckoutResult checkoutResult = repairOrderService.checkout("RO-SPRING-001");

        assertNotNull(checkoutResult);
        assertEquals(OrderStatus.COMPLETED, checkoutResult.getOrder().getStatus());

        PriceBreakdown price = checkoutResult.getPrice();
        assertEquals(new BigDecimal("2100.00"), price.getBasePrice());
        assertEquals(new BigDecimal("210.00"), price.getDiscountAmount());
        assertEquals(new BigDecimal("1890.00"), price.getFinalPrice());

        List<RepairOrder> clientOrders = repairOrderService.getOrdersByClientId("C-SPRING-1");
        assertEquals(1, clientOrders.size());
        assertEquals("RO-SPRING-001", clientOrders.get(0).getId());

        List<WorkItem> savedWorkItems = workItemRepository.findAllByRepairOrderId("RO-SPRING-001");
        assertEquals(1, savedWorkItems.size());
        assertEquals("Brake replacement", savedWorkItems.get(0).getDescription());
        assertEquals(WorkStatus.COMPLETED, savedWorkItems.get(0).getStatus());
    }

    @Test
    @DisplayName("Reloading order from DB loads together with works, parts, and current state (simulating reload/restart)")
    void reloadedOrder_LoadsTogetherWithWorksAndState_SimulatingRestart() {
        Client client = new Client("C-RELOAD-1", "Victor", "+380509990011", new BigDecimal("5.00"));
        clientRepository.save(client);

        Car car = new Car("1HGCR2F83HA112233", "BMW", "X5", 2021, client);
        carRepository.save(car);

        Mechanic mechanic = new Mechanic("M-RELOAD-1", "Oleg", MechanicSpecialization.GENERAL);
        mechanicRepository.save(mechanic);

        SparePart part1 = new SparePart("P-OIL-1", "Oil Castrol", new BigDecimal("1200.00"));
        sparePartRepository.save(part1);

        RepairOrder order = new RepairOrder("RO-RELOAD-001", car);
        repairOrderService.saveOrder(order);
        repairOrderService.diagnose("RO-RELOAD-001");

        WorkItem work1 = new WorkItem("Diagnostics", new BigDecimal("400.00"));
        WorkItem work2 = new WorkItem("Oil Change", new BigDecimal("350.00"), MechanicSpecialization.GENERAL, List.of(part1));
        repairOrderService.addWorks("RO-RELOAD-001", List.of(work1, work2));

        repairOrderService.approve("RO-RELOAD-001");
        repairOrderService.assignMechanic("RO-RELOAD-001", mechanic);
        repairOrderService.startProgress("RO-RELOAD-001");

        entityManager.flush();
        entityManager.clear();

        RepairOrder freshOrder = repairOrderService.getOrderById("RO-RELOAD-001");

        assertNotNull(freshOrder);
        assertEquals(OrderStatus.IN_PROGRESS, freshOrder.getStatus());
        assertEquals("BMW", freshOrder.getCar().getMake());
        assertEquals("Victor", freshOrder.getClient().getName());
        assertEquals("Oleg", freshOrder.getMechanic().getName());

        List<WorkItem> loadedWorks = freshOrder.getWorks();
        assertEquals(2, loadedWorks.size());
        assertEquals(new BigDecimal("750.00"), freshOrder.getLaborTotal());
        assertEquals(new BigDecimal("1200.00"), freshOrder.getPartsTotal());
        assertEquals(new BigDecimal("1950.00"), freshOrder.getTotalPrice());
    }

    @Test
    @DisplayName("completeOrder transactional operation saves status, completedAt, and final commercial price")
    void completeOrder_SavesStatusCompletedAtAndCommercialPrice() {
        Client client = new Client("C-COMP-1", "Alice", "+380509998877", new BigDecimal("20.00"));
        clientRepository.save(client);

        Car car = new Car("1HGCR2F83HA888888", "Toyota", "Camry", 2022, client);
        carRepository.save(car);

        Mechanic mechanic = new Mechanic("M-COMP-1", "Bob Pro", MechanicSpecialization.GENERAL);
        mechanicRepository.save(mechanic);

        RepairOrder order = new RepairOrder("RO-COMP-001", car);
        repairOrderService.saveOrder(order);
        repairOrderService.diagnose("RO-COMP-001");

        WorkItem work = new WorkItem("Oil change", new BigDecimal("1000.00"), MechanicSpecialization.GENERAL);
        repairOrderService.addWork("RO-COMP-001", work);
        repairOrderService.approve("RO-COMP-001");
        repairOrderService.assignMechanic("RO-COMP-001", mechanic);
        repairOrderService.startProgress("RO-COMP-001");
        repairOrderService.completeWork("RO-COMP-001", work);

        RepairOrder completedOrder = repairOrderService.completeOrder("RO-COMP-001");

        assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());
        assertNotNull(completedOrder.getCompletedAt());
        assertEquals(new BigDecimal("800.00"), completedOrder.getFinalCommercialPrice());
        assertTrue(mechanic.isAvailable());

        RepairOrder reloaded = repairOrderRepository.findById("RO-COMP-001").orElseThrow();
        assertEquals(OrderStatus.COMPLETED, reloaded.getStatus());
        assertNotNull(reloaded.getCompletedAt());
        assertEquals(new BigDecimal("800.00"), reloaded.getFinalCommercialPrice());
    }

    @Test
    @DisplayName("completeOrder rejects completion if work items are unfinished")
    void completeOrder_UnfinishedWorks_ThrowsIllegalStateException() {
        Client client = new Client("C-COMP-2", "Bob", "+380501114455", BigDecimal.ZERO);
        clientRepository.save(client);

        Car car = new Car("1HGCR2F83HA777777", "Ford", "Focus", 2018, client);
        carRepository.save(car);

        Mechanic mechanic = new Mechanic("M-COMP-2", "Carl", MechanicSpecialization.GENERAL);
        mechanicRepository.save(mechanic);

        RepairOrder order = new RepairOrder("RO-COMP-002", car);
        repairOrderService.saveOrder(order);
        repairOrderService.diagnose("RO-COMP-002");

        WorkItem work = new WorkItem("Diagnostics", new BigDecimal("500.00"));
        repairOrderService.addWork("RO-COMP-002", work);
        repairOrderService.approve("RO-COMP-002");
        repairOrderService.assignMechanic("RO-COMP-002", mechanic);
        repairOrderService.startProgress("RO-COMP-002");

        assertThrows(IllegalStateException.class, () -> repairOrderService.completeOrder("RO-COMP-002"));
    }

    @Test
    @DisplayName("Batch addWorks successfully persists all items in the batch")
    void addWorks_ValidBatch_AddsAllWorkItems() {
        Client client = new Client("C-BATCH-1", "Dmitro", "+380502223344", BigDecimal.ZERO);
        clientRepository.save(client);

        Car car = new Car("1HGCR2F83HA666666", "Mazda", "6", 2020, client);
        carRepository.save(car);

        RepairOrder order = new RepairOrder("RO-BATCH-001", car);
        repairOrderService.saveOrder(order);
        repairOrderService.diagnose("RO-BATCH-001");

        List<WorkItem> batch = List.of(
                new WorkItem("Front suspension check", new BigDecimal("400.00")),
                new WorkItem("Rear brake pad change", new BigDecimal("550.00")),
                new WorkItem("Cabin filter replacement", new BigDecimal("200.00"))
        );

        RepairOrder updated = repairOrderService.addWorks("RO-BATCH-001", batch);

        assertEquals(3, updated.getWorks().size());
        assertEquals(new BigDecimal("1150.00"), updated.getLaborTotal());
    }

    @Test
    @DisplayName("Batch addWorks rolls back completely if any element in the batch causes an error")
    void addWorks_ErrorInBatch_RollsBackAndSavesNoWorks() {
        Client client = new Client("C-BATCH-2", "Serhiy", "+380503334455", BigDecimal.ZERO);
        clientRepository.save(client);

        Car car = new Car("1HGCR2F83HA555555", "Audi", "A4", 2019, client);
        carRepository.save(car);

        RepairOrder order = new RepairOrder("RO-BATCH-002", car);
        repairOrderService.saveOrder(order);
        repairOrderService.diagnose("RO-BATCH-002");

        List<WorkItem> batchWithNull = new ArrayList<>();
        batchWithNull.add(new WorkItem("Suspension check", new BigDecimal("400.00")));
        batchWithNull.add(null);

        assertThrows(IllegalArgumentException.class, () ->
                repairOrderService.addWorks("RO-BATCH-002", batchWithNull)
        );

        RepairOrder checkOrder = repairOrderService.getOrderById("RO-BATCH-002");
        assertEquals(0, checkOrder.getWorks().size());
    }
}
