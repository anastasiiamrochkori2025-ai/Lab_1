package domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(of = "code")
public class SparePart {
    private String code;
    private String name;
    private String manufacturer;
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
}