package repository;

import domain.Car;
import domain.Client;
import domain.RepairOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryRepairOrderRepository Tests")
class InMemoryRepairOrderRepositoryTest {

    private InMemoryRepairOrderRepository repository;
    private RepairOrder order;
    private Client client;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepairOrderRepository();
        client = new Client("C-101", "Oleksandr Melnyk", "+380501234567", new BigDecimal("10.00"));
        Car car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2020, client);
        order = new RepairOrder("RO-001", car);
    }

    @Test
    @DisplayName("Should save and find order by identifier (findById)")
    void saveAndFindById_ValidOrder_StoresAndReturnsOrder() {
        repository.save(order);

        Optional<RepairOrder> found = repository.findById("RO-001");

        assertTrue(found.isPresent());
        assertEquals(order, found.get());
    }

    @Test
    @DisplayName("Searching for a non-existent ID returns Optional.empty()")
    void findById_MissingId_ReturnsEmptyOptional() {
        Optional<RepairOrder> found = repository.findById("UNKNOWN-ID");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should retrieve all orders for a specific client identifier (findAllByClientId)")
    void findAllByClientId_ExistingClient_ReturnsOnlyClientOrders() {
        repository.save(order);

        Client otherClient = new Client("C-102", "Maria", "+380671112233");
        Car otherCar = new Car("WVWZZZ1JZXW000001", "VW", "Golf", 2015, otherClient);
        repository.save(new RepairOrder("RO-002", otherCar));

        List<RepairOrder> clientOrders = repository.findAllByClientId(client.getId());

        assertEquals(1, clientOrders.size());
        assertEquals("RO-001", clientOrders.get(0).getId());
    }
}