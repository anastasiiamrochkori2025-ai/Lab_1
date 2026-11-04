package domain;

import validation.Validation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "work_items")
@Getter
@Setter
@ToString(exclude = {"repairOrder", "parts"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_order_id", foreignKey = @ForeignKey(name = "fk_work_items_repair_order"))
    private RepairOrder repairOrder;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "labor_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal laborCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_specialization", nullable = false, length = 32)
    private MechanicSpecialization requiredSpecialization;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(
            name = "work_item_parts",
            joinColumns = @JoinColumn(name = "work_item_id", foreignKey = @ForeignKey(name = "fk_wip_work_item")),
            inverseJoinColumns = @JoinColumn(name = "spare_part_code", foreignKey = @ForeignKey(name = "fk_wip_spare_part")),
            uniqueConstraints = @UniqueConstraint(name = "uk_work_item_part", columnNames = {"work_item_id", "spare_part_code"})
    )
    private List<SparePart> parts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WorkStatus status = WorkStatus.PENDING;

    public WorkItem(String description, BigDecimal laborCost) {
        this(description, laborCost, MechanicSpecialization.GENERAL, List.of());
    }

    public WorkItem(String description, BigDecimal laborCost, MechanicSpecialization requiredSpecialization) {
        this(description, laborCost, requiredSpecialization, List.of());
    }

    public WorkItem(String description,
                    BigDecimal laborCost,
                    MechanicSpecialization requiredSpecialization,
                    List<SparePart> parts) {
        this.description = Validation.requireText(description, "Description");
        this.laborCost = Validation.requireNonNegativeMoney(laborCost, "Labor cost");
        this.requiredSpecialization = Validation.requireNonNull(requiredSpecialization, "Required specialization");
        Validation.requireNonNull(parts, "Parts list");
        parts.forEach(part -> Validation.requireNonNull(part, "Spare part"));
        this.parts = new ArrayList<>(parts);
    }

    void markCompleted() {
        if (status == WorkStatus.COMPLETED) {
            throw new IllegalStateException("This work item is already completed.");
        }
        this.status = WorkStatus.COMPLETED;
    }

    public boolean isCompleted() {
        return status == WorkStatus.COMPLETED;
    }

    public BigDecimal getPartsCost() {
        if (parts == null || parts.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return parts.stream()
                .map(SparePart::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalCost() {
        return getPartsCost().add(laborCost).setScale(2, RoundingMode.HALF_UP);
    }

    public List<SparePart> getParts() {
        return parts == null ? List.of() : Collections.unmodifiableList(parts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkItem)) return false;
        WorkItem other = (WorkItem) o;
        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }
        return Objects.equals(description, other.description)
                && Objects.equals(laborCost, other.laborCost)
                && requiredSpecialization == other.requiredSpecialization;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, laborCost, requiredSpecialization);
    }
}
