package domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client Entity Tests")
class ClientTest {

    @Test
    @DisplayName("Creating a client without specifying a discount defaults to 0%")
    void constructor_WithoutDiscount_DefaultsToZero() {
        Client client = new Client("C-101", "Maria Shevchenko", "+380677654321");

        assertEquals("C-101", client.getId());
        assertEquals("Maria Shevchenko", client.getName());
        assertEquals("+380677654321", client.getPhone());
        assertEquals(new BigDecimal("0.00"), client.getDiscountPercentage());
    }

    @Test
    @DisplayName("Creating a client with an explicit personal discount")
    void constructor_WithValidDiscount_SetsDiscount() {
        Client client = new Client("C-102", "Oleksandr Melnyk", "+380501234567", new BigDecimal("15.50"));
        assertEquals(new BigDecimal("15.50"), client.getDiscountPercentage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.00", "100.01", "-15.00"})
    @DisplayName("Invalid discount percentage (< 0 or > 100) should throw IllegalArgumentException")
    void constructor_InvalidDiscount_ThrowsIllegalArgumentException(String invalidDiscount) {
        assertThrows(IllegalArgumentException.class,
                () -> new Client("C-103", "Ivan", "+380501112233", new BigDecimal(invalidDiscount)));
    }

    @Test
    @DisplayName("Blank name or phone number should throw IllegalArgumentException")
    void constructor_BlankFields_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Client("C-104", "  ", "+380501112233"));
        assertThrows(IllegalArgumentException.class,
                () -> new Client("C-104", "Ivan", "  "));
    }

    @Test
    @DisplayName("Updating discount percentage via setter with a valid value")
    void setDiscountPercentage_ValidValue_UpdatesDiscount() {
        Client client = new Client("C-105", "Ivan", "+380501112233");
        client.setDiscountPercentage(new BigDecimal("20.00"));

        assertEquals(new BigDecimal("20.00"), client.getDiscountPercentage());
    }
}