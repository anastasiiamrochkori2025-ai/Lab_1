package validation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Validation {

    private Validation() {
        throw new AssertionError("Utility class");
    }

    public static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank.");
        }
        return value.trim();
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null.");
        }
        return value;
    }

    public static BigDecimal requireNonNegativeMoney(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " cannot be null or negative.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
