package repository;

import domain.RepairOrder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryRepairOrderRepository implements RepairOrderRepository {
    private final Map<String, RepairOrder> storage = new LinkedHashMap<>();

    @Override
    public RepairOrder save(RepairOrder order) {
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<RepairOrder> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<RepairOrder> findAllByClientId(String clientId) {
        return storage.values().stream()
                .filter(order -> order.getClient().getId().equals(clientId))
                .toList();
    }
}
