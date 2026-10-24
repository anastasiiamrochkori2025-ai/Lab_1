package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Car Entity Tests")
class CarTest {

    private Client owner;

    @BeforeEach
    void setUp() {
        owner = new Client("C-01", "John Doe", "+380501234567", new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Should successfully create a car with valid data")
    void constructor_ValidData_CreatesCarSuccessfully() {
        Car car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2020, owner);

        assertEquals("1HGCR2F83HA123456", car.getVin());
        assertEquals("Honda", car.getMake());
        assertEquals("Accord", car.getModel());
        assertEquals(2020, car.getYear());
        assertEquals(owner, car.getOwner());
    }

    @Test
    @DisplayName("Boundary case: minimum allowed year under modern VIN standard (1981) is valid")
    void constructor_BoundaryYear1981_CreatesCarSuccessfully() {
        Car car = new Car("1HGCR2F83HA123456", "Honda", "Accord", 1981, owner);
        assertEquals(1981, car.getYear());
    }

    @Test
    @DisplayName("Should automatically convert lowercase VIN to uppercase")
    void constructor_LowercaseVin_ConvertsToUpperCase() {
        Car car = new Car("1hgcr2f83ha123456", "Honda", "Accord", 2020, owner);
        assertEquals("1HGCR2F83HA123456", car.getVin());
    }

    @ParameterizedTest
    @ValueSource(ints = {1970, 1980})
    @DisplayName("Should throw IllegalArgumentException when manufacturing year is before 1981")
    void constructor_YearBefore1981_ThrowsIllegalArgumentException(int invalidYear) {
        assertThrows(IllegalArgumentException.class,
                () -> new Car("1HGCR2F83HA123456", "Honda", "Accord", invalidYear, owner));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when manufacturing year is in the distant future (> current year + 1)")
    void constructor_FutureYear_ThrowsIllegalArgumentException() {
        int futureYear = Year.now().getValue() + 2;
        assertThrows(IllegalArgumentException.class,
                () -> new Car("1HGCR2F83HA123456", "Honda", "Accord", futureYear, owner));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when VIN is blank")
    void constructor_BlankVin_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Car("   ", "Honda", "Accord", 2020, owner));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when owner is null")
    void constructor_NullOwner_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Car("1HGCR2F83HA123456", "Honda", "Accord", 2020, null));
    }
}