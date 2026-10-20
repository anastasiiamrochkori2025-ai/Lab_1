package domain;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@ToString
public class WorkItem {
    private String description;
    private BigDecimal laborCost;
    private MechanicSpecialization requiredSpecialization;
    private List<SparePart> parts;
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
        for (SparePart part : parts) {
            Validation.requireNonNull(part, "Spare part");
        }
        this.parts = List.copyOf(parts);
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
        BigDecimal total = BigDecimal.ZERO;
        for (SparePart part : parts) {
            total = total.add(part.getPrice());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalCost() {
        return getPartsCost().add(laborCost).setScale(2, RoundingMode.HALF_UP);
    }
}