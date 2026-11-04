package domain;

import validation.Validation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Year;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "cars")
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Car {
    private static final int MIN_VIN_STANDARD_YEAR = 1981;

    @Id
    @Column(name = "vin", nullable = false, length = 17, unique = true)
    private String vin;

    @Column(name = "make", nullable = false, length = 100)
    private String make;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "car_year", nullable = false)
    private int year;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cars_owner"))
    @ToString.Exclude
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(vin, car.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin);
    }
}
