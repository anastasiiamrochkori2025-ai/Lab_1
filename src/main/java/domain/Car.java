package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Year;
import java.util.Locale;

@Data
@EqualsAndHashCode(of = "vin")
public class Car {
    private static final int MIN_VIN_STANDARD_YEAR = 1981;

    private final String vin;
    private String make;
    private String model;
    private int year;
    private Client owner;

    public Car(String vin, String make, String model, int year, Client owner) {
        int maxAllowedYear = Year.now().getValue() + 1;
        if (year < MIN_VIN_STANDARD_YEAR || year > maxAllowedYear) {
            throw new IllegalArgumentException(
                    "Year must be between " + MIN_VIN_STANDARD_YEAR + " and " + maxAllowedYear + ".");
        }
        this.vin = Validation.requireText(vin, "VIN").toUpperCase(Locale.ROOT);
        this.make = Validation.requireText(make, "Make");
        this.model = Validation.requireText(model, "Model");
        this.year = year;
        this.owner = Validation.requireNonNull(owner, "Owner");
    }
}