package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.AccessLevel;

@Data
@EqualsAndHashCode(of = "id")
public class Mechanic {
    private final String id;
    private String name;
    private MechanicSpecialization specialization;

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
}