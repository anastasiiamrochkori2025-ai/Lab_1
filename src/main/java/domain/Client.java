package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@EqualsAndHashCode(of = "id")
public class Client {
    private static final BigDecimal MAX_DISCOUNT = new BigDecimal("100");

    private final String id;
    private String name;
    private String phone;
    private BigDecimal discountPercentage;

    public Client(String id, String name, String phone) {
        this(id, name, phone, BigDecimal.ZERO);
    }

    public Client(String id, String name, String phone, BigDecimal discountPercentage) {
        this.id = Validation.requireText(id, "Client id");
        this.name = Validation.requireText(name, "Client name");
        this.phone = Validation.requireText(phone, "Client phone");
        this.discountPercentage = validateDiscount(discountPercentage);
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = validateDiscount(discountPercentage);
    }

    private static BigDecimal validateDiscount(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(MAX_DISCOUNT) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}