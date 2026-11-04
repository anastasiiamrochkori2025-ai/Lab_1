package domain;

import validation.Validation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Entity
@Table(name = "mechanics")
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mechanic {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialization", nullable = false, length = 32)
    private MechanicSpecialization specialization;

    @Column(name = "available", nullable = false)
    @Setter(AccessLevel.NONE)
    private boolean available = true;

    public Mechanic(String id, String name, MechanicSpecialization specialization) {
        this.id = Validation.requireText(id, "Mechanic id");
        this.name = Validation.requireText(name, "Mechanic name");
        this.specialization = Validation.requireNonNull(specialization, "Specialization");
    }

    public void makeBusy() {
        if (!available) {
            throw new IllegalStateException("The mechanic is already busy.");
        }
        this.available = false;
    }

    public void makeFree() {
        if (available) {
            throw new IllegalStateException("The mechanic is already available.");
        }
        this.available = true;
    }

    public boolean canPerform(WorkItem work) {
        if (work == null) {
            return false;
        }
        MechanicSpecialization required = work.getRequiredSpecialization();
        return specialization == MechanicSpecialization.GENERAL
                || required == MechanicSpecialization.GENERAL
                || specialization == required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mechanic mechanic = (Mechanic) o;
        return Objects.equals(id, mechanic.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
