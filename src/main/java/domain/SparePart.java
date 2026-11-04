package domain;

import validation.Validation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "spare_parts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SparePart {

    @Id
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "manufacturer", nullable = false, length = 255)
    private String manufacturer;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public SparePart(String code, String name, BigDecimal price) {
        this(code, name, "Generic", price);
    }

    public SparePart(String code, String name, String manufacturer, BigDecimal price) {
        this.code = Validation.requireText(code, "Spare part code");
        this.name = Validation.requireText(name, "Spare part name");
        this.manufacturer = Validation.requireText(manufacturer, "Manufacturer");
        this.price = Validation.requireNonNegativeMoney(price, "Spare part price");
    }

    @Override
    public String toString() {
        return name + " [" + code + "] (" + price + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SparePart sparePart = (SparePart) o;
        return Objects.equals(code, sparePart.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
