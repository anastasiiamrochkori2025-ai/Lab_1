package domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SparePart Domain Class Tests")
class SparePartTest {

    @Test
    @DisplayName("Should successfully create a spare part with complete valid data")
    void constructor_ValidData_InitializesCorrectly() {
        SparePart part = new SparePart("BP-204", "Brake Pads", "Brembo", new BigDecimal("1850.00"));

        assertEquals("BP-204", part.getCode());
        assertEquals("Brake Pads", part.getName());
        assertEquals("Brembo", part.getManufacturer());
        assertEquals(new BigDecimal("1850.00"), part.getPrice());
    }

    @Test
    @DisplayName("Creating a spare part without a manufacturer defaults to 'Generic'")
    void constructor_WithoutManufacturer_DefaultsToGeneric() {
        SparePart part = new SparePart("OIL-1", "Engine Oil", new BigDecimal("800.00"));

        assertEquals("Generic", part.getManufacturer());
    }

    @Test
    @DisplayName("Negative spare part price should throw IllegalArgumentException")
    void constructor_NegativePrice_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new SparePart("ERR-1", "Part", new BigDecimal("-100.00")));
    }

    @Test
    @DisplayName("Spare parts with the same code should be equal according to equals and hashCode contracts")
    void equalsAndHashCode_SameCode_ReturnsTrue() {
        SparePart part1 = new SparePart("BP-204", "Brake Pads", "Brembo", new BigDecimal("1850.00"));
        SparePart part2 = new SparePart("BP-204", "Alternative Name", "Bosch", new BigDecimal("1500.00"));

        assertEquals(part1, part2);
        assertEquals(part1.hashCode(), part2.hashCode());
    }
}