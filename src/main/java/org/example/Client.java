package org.example;

public final class Client {
    private final String id;
    private final String name;
    private final String phone;

    public Client(String id, String name, String phone) {
        this.id = requireText(id, "Client id cannot be null or blank.");
        this.name = requireText(name, "Client name cannot be null or blank.");
        this.phone = requireText(phone, "Client phone cannot be null or blank.");
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

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}