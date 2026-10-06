package org.example;

public final class Client {
    private final String id;
    private final String name;
    private final String phone;

    public Client(String id, String name, String phone) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Client id cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name cannot be null or blank.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Client phone cannot be null or blank.");
        }
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}