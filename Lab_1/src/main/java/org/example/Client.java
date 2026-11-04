package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Client {
    private String id;
    private String name;
    private String phone;
    private BigDecimal discountPercentage;

    public Client(String id, String name, String phone) {
        this(id, name, phone, BigDecimal.ZERO);
    }

    public Client(String id, String name, String phone, BigDecimal discountPercentage) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Client id cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name cannot be null or blank.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Client phone cannot be null or blank.");
        }
        if (discountPercentage == null
                || discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }

        this.id = id;
        this.name = name;
        this.phone = phone;
        this.discountPercentage = discountPercentage.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        if (discountPercentage == null
                || discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage.setScale(2, RoundingMode.HALF_UP);
    }
}