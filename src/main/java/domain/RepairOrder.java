package domain;

import validation.Validation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "repair_orders")
@Getter
@Setter
@ToString(exclude = "works")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairOrder {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_vin", nullable = false, foreignKey = @ForeignKey(name = "fk_repair_orders_car"))
    private Car car;

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<WorkItem> works = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status = OrderStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id", foreignKey = @ForeignKey(name = "fk_repair_orders_mechanic"))
    private Mechanic mechanic;

    @Column(name = "final_commercial_price", precision = 10, scale = 2)
    private BigDecimal finalCommercialPrice;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public RepairOrder(String id, Car car) {
        this.id = Validation.requireText(id, "Order id");
        this.car = Validation.requireNonNull(car, "Car");
    }

    public void diagnose() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Diagnosis can only be performed on newly created orders.");
        }
        this.status = OrderStatus.DIAGNOSED;
    }

    public void addWork(WorkItem work) {
        if (status != OrderStatus.DIAGNOSED) {
            throw new IllegalStateException("Work can only be added to a diagnosed order.");
        }
        Validation.requireNonNull(work, "Work item");
        work.setRepairOrder(this);
        this.works.add(work);
    }

    public void approve() {
        if (status != OrderStatus.DIAGNOSED) {
            throw new IllegalStateException("Only diagnosed orders can be approved.");
        }
        if (works.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one work item to be approved.");
        }
        this.status = OrderStatus.APPROVED;
    }

    public void assignMechanic(Mechanic mechanic) {
        if (status != OrderStatus.APPROVED) {
            throw new IllegalStateException("A mechanic can only be assigned to an approved order.");
        }
        Validation.requireNonNull(mechanic, "Mechanic");
        if (this.mechanic != null) {
            throw new IllegalStateException("A mechanic is already assigned to this order.");
        }
        if (!mechanic.isAvailable()) {
            throw new IllegalStateException("The mechanic is currently busy.");
        }
        works.stream()
                .filter(work -> !mechanic.canPerform(work))
                .findFirst()
                .ifPresent(work -> {
                    throw new IllegalStateException(
                            "The mechanic does not have the required specialization for: " + work.getDescription());
                });
        mechanic.makeBusy();
        this.mechanic = mechanic;
    }

    public void startProgress() {
        if (status != OrderStatus.APPROVED) {
            throw new IllegalStateException("Only approved orders can be started.");
        }
        if (mechanic == null) {
            throw new IllegalStateException("A mechanic must be assigned before starting work.");
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    public void completeWork(WorkItem work) {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Work can only be completed while the order is in progress.");
        }
        Validation.requireNonNull(work, "Work item");
        WorkItem target = works.stream()
                .filter(w -> Objects.equals(w, work))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Work item does not belong to this order."));
        target.markCompleted();
        if (work != target) {
            work.markCompleted();
        }
    }

    public void complete() {
        complete(getTotalPrice(), LocalDateTime.now());
    }

    public void complete(BigDecimal finalCommercialPrice, LocalDateTime completedAt) {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress orders can be completed.");
        }
        works.stream()
                .filter(work -> !work.isCompleted())
                .findFirst()
                .ifPresent(work -> {
                    throw new IllegalStateException("All work items must be completed first: " + work.getDescription());
                });
        Validation.requireNonNull(finalCommercialPrice, "Final commercial price");
        Validation.requireNonNull(completedAt, "Completed at timestamp");
        if (finalCommercialPrice.signum() < 0) {
            throw new IllegalArgumentException("Final commercial price cannot be negative.");
        }
        if (mechanic != null) {
            mechanic.makeFree();
        }
        this.status = OrderStatus.COMPLETED;
        this.finalCommercialPrice = finalCommercialPrice.setScale(2, RoundingMode.HALF_UP);
        this.completedAt = completedAt;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Completed or cancelled orders cannot be cancelled.");
        }
        if (mechanic != null) {
            mechanic.makeFree();
            this.mechanic = null;
        }
        this.status = OrderStatus.CANCELLED;
    }

    public BigDecimal getLaborTotal() {
        return works.stream()
                .map(WorkItem::getLaborCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPartsTotal() {
        return works.stream()
                .map(WorkItem::getPartsCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPrice() {
        return getLaborTotal().add(getPartsTotal()).setScale(2, RoundingMode.HALF_UP);
    }

    public Client getClient() {
        return car.getOwner();
    }

    public List<WorkItem> getWorks() {
        return Collections.unmodifiableList(works);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepairOrder that = (RepairOrder) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
