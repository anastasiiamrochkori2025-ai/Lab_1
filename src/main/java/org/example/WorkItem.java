package org.example;

import java.math.BigDecimal;

public final class WorkItem {
    private final String description;
    private final BigDecimal partsCost;
    private final BigDecimal laborCost;
    private final WorkStatus status;

    public WorkItem(String description, BigDecimal partsCost, BigDecimal laborCost) {
        this(description, partsCost, laborCost, WorkStatus.PENDING);
    }

    public WorkItem(String description, BigDecimal partsCost, BigDecimal laborCost, WorkStatus status) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank.");
        }
        if (partsCost == null || partsCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Parts cost cannot be null or negative.");
        }
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Labor cost cannot be null or negative.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Work status cannot be null.");
        }

        this.description = description;
        this.partsCost = partsCost;
        this.laborCost = laborCost;
        this.status = status;
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
}