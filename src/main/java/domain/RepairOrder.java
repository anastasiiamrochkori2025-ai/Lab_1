package domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@ToString(exclude = "works")
@EqualsAndHashCode(of = "id")
public class RepairOrder {
    private String id;
    private Car car;

    @Getter(AccessLevel.NONE)
    private final List<WorkItem> works = new ArrayList<>();

    private OrderStatus status = OrderStatus.CREATED;
    private Mechanic mechanic;

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
        for (WorkItem work : works) {
            if (!mechanic.canPerform(work)) {
                throw new IllegalStateException(
                        "The mechanic does not have the required specialization for: " + work.getDescription());
            }
        }
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
        if (!works.contains(work)) {
            throw new IllegalArgumentException("Work item does not belong to this order.");
        }
        work.markCompleted();
    }

    public void complete() {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress orders can be completed.");
        }
        for (WorkItem work : works) {
            if (!work.isCompleted()) {
                throw new IllegalStateException("All work items must be completed first: " + work.getDescription());
            }
        }
        mechanic.makeFree();
        this.status = OrderStatus.COMPLETED;
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
        BigDecimal total = BigDecimal.ZERO;
        for (WorkItem work : works) {
            total = total.add(work.getLaborCost());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPartsTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (WorkItem work : works) {
            total = total.add(work.getPartsCost());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
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
}