package repository;

import domain.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, String> {
    List<SparePart> findAllByManufacturer(String manufacturer);
}
