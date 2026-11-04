package domain;

import validation.Validation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(
        name = "clients",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_clients_phone", columnNames = {"phone"})
        }
)
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Client {
    private static final BigDecimal MAX_DISCOUNT = new BigDecimal("100");

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "phone", nullable = false, unique = true, length = 50)
    private String phone;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
