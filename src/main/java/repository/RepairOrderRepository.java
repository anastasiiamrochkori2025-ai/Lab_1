package repository;

import domain.RepairOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, String> {

    @Override
    @EntityGraph(attributePaths = {"works", "car", "car.owner", "mechanic"})
    Optional<RepairOrder> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"works", "car", "car.owner", "mechanic"})
    List<RepairOrder> findAll();

    @EntityGraph(attributePaths = {"works", "car", "car.owner", "mechanic"})
    @Query("SELECT r FROM RepairOrder r WHERE r.car.owner.id = :clientId")
    List<RepairOrder> findAllByClientId(@Param("clientId") String clientId);
}
