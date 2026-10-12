package org.example;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SparePartWarehouse {
    private final Map<String, SparePart> parts;
    private final Map<String, Integer> quantities;

    public SparePartWarehouse() {
        this.parts = new LinkedHashMap<>();
        this.quantities = new LinkedHashMap<>();
    }

    public void addPart(SparePart part, int quantity) {
        if (part == null) {
            throw new IllegalArgumentException("Spare part cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        String code = normalizeCode(part.getCode());
        boolean registered = parts.containsKey(code);
        if (!registered) {
            parts.put(code, part);
        }

        int currentQuantity = quantities.getOrDefault(code, 0);
        quantities.put(code, currentQuantity + quantity);
    }

    public void issuePart(String code, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        String normalizedCode = normalizeCode(code);
        int currentQuantity = getQuantity(code);
        if (currentQuantity < quantity) {
            throw new IllegalStateException("Not enough spare parts in warehouse.");
        }

        int newQuantity = currentQuantity - quantity;
        quantities.put(normalizedCode, newQuantity);
    }

    public int getQuantity(String code) {
        String normalizedCode = normalizeCode(code);
        Integer quantity = quantities.get(normalizedCode);
        if (quantity == null) {
            throw new IllegalArgumentException("Spare part with code " + code + " is not found.");
        }
        return quantity;
    }

    public Map<SparePart, Integer> getInventory() {
        Map<SparePart, Integer> inventory = new LinkedHashMap<>();
        for (String code : parts.keySet()) {
            SparePart part = parts.get(code);
            Integer quantity = quantities.get(code);
            inventory.put(part, quantity);
        }
        return Collections.unmodifiableMap(inventory);
    }

    public BigDecimal calculateTotalValue() {
        BigDecimal totalValue = BigDecimal.ZERO;
        for (String code : parts.keySet()) {
            SparePart part = parts.get(code);
            int quantity = quantities.get(code);
            BigDecimal itemValue = part.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalValue = totalValue.add(itemValue);
        }
        return totalValue;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Spare part code cannot be null or blank.");
        }
        return code.trim().replaceAll("\s+", " ").toUpperCase();
    }
}
