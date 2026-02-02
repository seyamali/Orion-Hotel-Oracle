package com.orionhotel.controller;

import com.orionhotel.model.InventoryItem;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {

    private static final String DATA_FILE = "inventory_db.ser";

    // Track consumption events for reporting
    public static class ConsumptionEvent implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final int itemId;
        public final String itemName;
        public final int amount;
        public final java.time.LocalDateTime timestamp;

        public ConsumptionEvent(int itemId, String itemName, int amount, java.time.LocalDateTime timestamp) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    // Track restock events
    public static class RestockEvent implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final int itemId;
        public final String itemName;
        public final int amount;
        public final java.time.LocalDateTime timestamp;

        public RestockEvent(int itemId, String itemName, int amount, java.time.LocalDateTime timestamp) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    // Track low stock alerts
    public static class LowStockEvent implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final int itemId;
        public final String itemName;
        public final int currentQty;
        public final int threshold;
        public final java.time.LocalDateTime timestamp;

        public LowStockEvent(int itemId, String itemName, int currentQty, int threshold,
                java.time.LocalDateTime timestamp) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.currentQty = currentQty;
            this.threshold = threshold;
            this.timestamp = timestamp;
        }
    }

    private List<ConsumptionEvent> consumptionEvents = new ArrayList<>();
    private List<RestockEvent> restockEvents = new ArrayList<>();
    private List<LowStockEvent> lowStockEvents = new ArrayList<>();

    private List<InventoryItem> inventory = new ArrayList<>();

    public InventoryController() {
        loadData();
    }

    public void addItem(InventoryItem item) {
        inventory.add(item);
        saveData();
    }

    public List<InventoryItem> getAllItems() {
        return inventory;
    }

    public boolean consumeItem(int itemId, int amount) {
        for (InventoryItem item : inventory) {
            if (item.getItemId() == itemId) {
                if (item.getQuantity() >= amount) {
                    item.setQuantity(item.getQuantity() - amount);
                    consumptionEvents.add(
                            new ConsumptionEvent(itemId, item.getItemName(), amount, java.time.LocalDateTime.now()));

                    // Check for low stock
                    if (item.getQuantity() <= item.getMinimumThreshold()) {
                        lowStockEvents.add(new LowStockEvent(itemId, item.getItemName(), item.getQuantity(),
                                item.getMinimumThreshold(), java.time.LocalDateTime.now()));
                    }
                    saveData();
                    return true;
                }
            }
        }
        return false;
    }

    public void restockItem(int itemId, int amount) {
        for (InventoryItem item : inventory) {
            if (item.getItemId() == itemId) {
                item.setQuantity(item.getQuantity() + amount);
                restockEvents.add(new RestockEvent(itemId, item.getItemName(), amount, java.time.LocalDateTime.now()));
                saveData();
            }
        }
    }

    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> low = new ArrayList<>();
        for (InventoryItem item : inventory) {
            if (item.getQuantity() <= item.getMinimumThreshold()) {
                low.add(item);
            }
        }
        return low;
    }

    public List<ConsumptionEvent> getConsumptionEventsForDay(java.time.LocalDate date) {
        List<ConsumptionEvent> result = new ArrayList<>();
        for (ConsumptionEvent e : consumptionEvents) {
            if (e.timestamp.toLocalDate().equals(date)) {
                result.add(e);
            }
        }
        return result;
    }

    public List<RestockEvent> getRestockEventsForMonth(java.time.YearMonth month) {
        List<RestockEvent> result = new ArrayList<>();
        for (RestockEvent e : restockEvents) {
            if (java.time.YearMonth.from(e.timestamp).equals(month)) {
                result.add(e);
            }
        }
        return result;
    }

    public List<LowStockEvent> getLowStockHistory() {
        return new ArrayList<>(lowStockEvents);
    }

    // Returns a list of simple DTOs or just strings for the "Most Used" report
    // For simplicity, returning a list of ConsumptionEvents aggregated by item, or
    // just the raw list for the UI to process.
    // Let's return a specific DTO for the UI to display: Item Name -> Total
    // Consumed
    public static class ItemUsage {
        public final String itemName;
        public final int totalConsumed;

        public ItemUsage(String name, int total) {
            this.itemName = name;
            this.totalConsumed = total;
        }
    }

    public List<ItemUsage> getMostUsedItems() {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (ConsumptionEvent e : consumptionEvents) {
            counts.put(e.itemName, counts.getOrDefault(e.itemName, 0) + e.amount);
        }
        List<ItemUsage> list = new ArrayList<>();
        counts.forEach((k, v) -> list.add(new ItemUsage(k, v)));
        list.sort((a, b) -> Integer.compare(b.totalConsumed, a.totalConsumed)); // Descending
        return list;
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        java.io.File file = new java.io.File(DATA_FILE);
        if (file.exists()) {
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                inventory = (List<InventoryItem>) ois.readObject();
                consumptionEvents = (List<ConsumptionEvent>) ois.readObject();
                restockEvents = (List<RestockEvent>) ois.readObject();
                lowStockEvents = (List<LowStockEvent>) ois.readObject();
                System.out.println("Data loaded successfully from " + DATA_FILE);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to load data: " + e.getMessage());
            }
        } else {
            System.out.println("No existing data file found. Starting fresh.");
        }
    }

    private void saveData() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(DATA_FILE))) {
            oos.writeObject(inventory);
            oos.writeObject(consumptionEvents);
            oos.writeObject(restockEvents);
            oos.writeObject(lowStockEvents);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }
}