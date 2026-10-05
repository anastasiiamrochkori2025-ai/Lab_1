package org.example;

import java.math.BigDecimal;

public final class WorkItem {
    private final String description;
    private final BigDecimal partsCost;
    private final BigDecimal laborCost;
    private WorkStatus status;

    public WorkItem(String description, BigDecimal partsCost, BigDecimal laborCost) {
        this.description = requireDescription(description);
        this.partsCost = requireNonNegative(partsCost, "Parts cost cannot be null or negative.");
        this.laborCost = requireNonNegative(laborCost, "Labor cost cannot be null or negative.");
        this.status = WorkStatus.PENDING;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public boolean isCompleted() {
        return status == WorkStatus.COMPLETED;
    }

    public void completeWork() {
        if (status == WorkStatus.COMPLETED) {
            throw new IllegalStateException("Work item is already completed.");
        }
        status = WorkStatus.COMPLETED;
    }

    public BigDecimal getTotalCost() {
        return partsCost.add(laborCost);
    }

    private static String requireDescription(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Work description cannot be null or blank.");
        }
        return value;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}