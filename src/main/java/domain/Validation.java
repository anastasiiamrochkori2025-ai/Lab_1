package domain;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
final class Validation {

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank.");
        }
        return value.trim();
    }

    static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null.");
        }
        return value;
    }

    static BigDecimal requireNonNegativeMoney(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " cannot be null or negative.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}