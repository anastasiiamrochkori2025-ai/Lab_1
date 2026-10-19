package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepairOrderRepository implements RepairOrderRepository {
    private Map<String, RepairOrder> storage;

    public InMemoryRepairOrderRepository() {
        this.storage = new ConcurrentHashMap<>();
    }

    public InMemoryRepairOrderRepository(Map<String, RepairOrder> initialStorage) {
        if (initialStorage == null) {
            throw new IllegalArgumentException("Initial storage cannot be null.");
        }
        this.storage = new ConcurrentHashMap<>();
        for (Map.Entry<String, RepairOrder> entry : initialStorage.entrySet()) {
            if (entry.getValue() != null) {
                this.storage.put(entry.getKey(), copy(entry.getValue()));
            }
        }
    }

    @Override
    public RepairOrder save(RepairOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Repair order cannot be null.");
        }
        String id = order.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Repair order id cannot be null or blank.");
        }

        // Defensive copy on save to prevent external mutation of internal repository state
        RepairOrder storedCopy = copy(order);
        storage.put(id, storedCopy);

        // Defensive copy on return
        return copy(storedCopy);
    }

    @Override
    public Optional<RepairOrder> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id cannot be null or blank.");
        }

        RepairOrder order = storage.get(id);
        if (order == null) {
            return Optional.empty();
        }

        // Defensive copy on read so external modifications do not corrupt internal state
        return Optional.of(copy(order));
    }

    @Override
    public List<RepairOrder> findAllByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client id cannot be null or blank.");
        }

        List<RepairOrder> matchingOrders = new ArrayList<>();
        for (RepairOrder order : storage.values()) {
            if (order.getClient() != null && clientId.equals(order.getClient().getId())) {
                matchingOrders.add(copy(order));
            }
        }

        return Collections.unmodifiableList(matchingOrders);
    }

    private RepairOrder copy(RepairOrder order) {
        if (order == null) {
            return null;
        }
        return new RepairOrder(order);
    }
}
