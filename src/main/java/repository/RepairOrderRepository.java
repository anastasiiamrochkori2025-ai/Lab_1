package repository;

import domain.RepairOrder;

import java.util.List;
import java.util.Optional;

public interface RepairOrderRepository {

    RepairOrder save(RepairOrder order);
    Optional<RepairOrder> findById(String id);
    List<RepairOrder> findAllByClientId(String clientId);
}
