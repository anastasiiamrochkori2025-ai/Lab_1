package org.example;

import java.math.BigDecimal;

public final class SparePart {
    private final String code;
    private String name;
    private BigDecimal price;

    public SparePart(String code, String name, String manufacturer, BigDecimal price) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Spare part code cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Spare part name cannot be null or blank.");
        }
        if (manufacturer == null || manufacturer.isBlank()) {
            throw new IllegalArgumentException("Manufacturer cannot be null or blank.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Spare part price cannot be null or negative.");
        }

        this.code = code;
        this.name = name;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
