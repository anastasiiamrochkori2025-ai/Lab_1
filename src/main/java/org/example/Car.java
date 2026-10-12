package org.example;
import java.time.Year;

public final class Car {
    private static final int MIN_VIN_STANDARD_YEAR = 1981;
    private final String vin;
    private final String make;
    private final String model;
    private final int year;
    private Client owner;

    public Car(String vin, String make, String model, int year, Client owner) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("VIN cannot be null or blank.");
        }
        if (make == null || make.isBlank()) {
            throw new IllegalArgumentException("Make cannot be null or blank.");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model cannot be null or blank.");
        }
        int maxAllowedYear = Year.now().getValue() + 1;
        if (year < MIN_VIN_STANDARD_YEAR || year > maxAllowedYear) {
            throw new IllegalArgumentException(
                    "Year must be between " + MIN_VIN_STANDARD_YEAR + " and " + maxAllowedYear + "."
            );
        }
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null.");
        }

        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.owner = owner;
    }

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public Client getOwner() {
        return owner;
    }
}