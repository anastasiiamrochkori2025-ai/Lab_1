package domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Utility Class Tests")
class ValidationTest {

    @Test
    @DisplayName("requireText returns a trimmed string for valid input")
    void requireText_ValidText_ReturnsTrimmedString() {
        String result = Validation.requireText("   test   ", "Field");
        assertEquals("test", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("requireText throws IllegalArgumentException for null, empty, or blank strings")
    void requireText_BlankOrNull_ThrowsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class, () -> Validation.requireText(input, "Field"));
    }

    @Test
    @DisplayName("requireNonNull returns the object or throws IllegalArgumentException when null")
    void requireNonNull_HandlesCorrectly() {
        Object obj = new Object();
        assertSame(obj, Validation.requireNonNull(obj, "Field"));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireNonNull(null, "Field"));
    }

    @Test
    @DisplayName("requireNonNegativeMoney rounds to 2 decimal places or throws exception for negative/null values")
    void requireNonNegativeMoney_HandlesMoneyCorrectly() {
        BigDecimal valid = Validation.requireNonNegativeMoney(new BigDecimal("10.555"), "Money");
        assertEquals(new BigDecimal("10.56"), valid);

        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireNonNegativeMoney(new BigDecimal("-0.01"), "Money"));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireNonNegativeMoney(null, "Money"));
    }
}