package org.example;

public final class Mechanic {
    private final String id;
    private final String name;
    private final MechanicSpecialization specialization;
    private boolean busy;

    public Mechanic(String id, String name, MechanicSpecialization specialization) {
        this.id = requireText(id, "Mechanic id cannot be null or blank.");
        this.name = requireText(name, "Mechanic name cannot be null or blank.");
        this.specialization = requireNotNull(specialization, "Mechanic specialization cannot be null.");
        this.busy = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MechanicSpecialization getSpecialization() {
        return specialization;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean hasSameId(Mechanic mechanic) {
        Mechanic checkedMechanic = requireNotNull(mechanic, "Mechanic cannot be null.");
        return id.equals(checkedMechanic.id);
    }

    public void assignToOrder() {
        if (busy) {
            throw new IllegalStateException("Mechanic is already busy.");
        }
        busy = true;
    }

    public void releaseFromOrder() {
        busy = false;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static <T> T requireNotNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}