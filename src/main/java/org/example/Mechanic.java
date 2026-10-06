package org.example;

public final class Mechanic {
    private final String id;
    private final String name;
    private final MechanicSpecialization specialization;

    public Mechanic(String id, String name, MechanicSpecialization specialization) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mechanic id cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Mechanic name cannot be null or blank.");
        }
        if (specialization == null) {
            throw new IllegalArgumentException("Specialization cannot be null.");
        }
        this.id = id;
        this.name = name;
        this.specialization = specialization;
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
}