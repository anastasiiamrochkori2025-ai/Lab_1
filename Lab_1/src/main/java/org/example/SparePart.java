package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SparePart {
    private String code;
    private String name;
    private String manufacturer;
    private BigDecimal price;

    public SparePart(String code, String name, BigDecimal price) {
        this(code, name, "Generic", price);
    }

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
        this.manufacturer = manufacturer;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SparePart sparePart = (SparePart) o;
        return code.equals(sparePart.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return name + " [" + code + "] (" + price + " UAH)";
    }
}
