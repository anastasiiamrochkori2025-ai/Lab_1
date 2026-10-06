package org.example;

public final class Car {
    private final String vin;
    private final String make;
    private final String model;
    private final int year;
    private final Client owner;

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
        if (year < 1886) {
            throw new IllegalArgumentException("Year cannot be earlier than 1886.");
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