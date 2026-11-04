package repository;

import domain.Mechanic;
import domain.MechanicSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, String> {
    List<Mechanic> findAllByAvailableTrue();
    List<Mechanic> findAllBySpecialization(MechanicSpecialization specialization);
}
