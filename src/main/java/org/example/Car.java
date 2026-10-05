package org.example;

import java.time.Year;
import java.util.Locale;

public final class Car {
    private final String vin;
    private final String make;
    private final String model;
    private final int year;
    private final Client owner;

    public Car(String vin, String make, String model, int year, Client owner) {
        this.vin = requireText(vin, "VIN cannot be null or blank.");
        this.make = requireText(make, "Car make cannot be null or blank.");
        this.model = requireText(model, "Car model cannot be null or blank.");
        this.year = requireValidYear(year);
        this.owner = requireNotNull(owner, "Car owner cannot be null.");
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

    public boolean hasSameVin(Car other) {
        Car checkedCar = requireNotNull(other, "Car cannot be null.");
        return vin.equalsIgnoreCase(checkedCar.vin);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Car other)) {
            return false;
        }

        return hasSameVin(other);
    }

    @Override
    public int hashCode() {
        String normalizedVin = vin.toUpperCase(Locale.ROOT);
        return normalizedVin.hashCode();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static int requireValidYear(int year) {
        Year currentYear = Year.now();
        int nextYear = currentYear.getValue() + 1;

        if (year < 1886 || year > nextYear) {
            throw new IllegalArgumentException("Car year must be between 1886 and " + nextYear + ".");
        }
        return year;
    }

    private static <T> T requireNotNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
