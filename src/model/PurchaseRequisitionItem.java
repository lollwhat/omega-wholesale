package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PurchaseRequisitionItem {
    private String prId;
    private String itemId; // This is the ItemEntryId (e.g., IM001)
    private String itemCode;
    private String itemName;
    private int quantity;
    private int price;
    private int totalPrice;
    private List<String> suggestedSupplierIds; // New field

    // Constructor for creating a new item, typically when suggestedSupplierIds are known
    public PurchaseRequisitionItem(String prId, String itemId, String itemCode, String itemName, int quantity, int price, List<String> suggestedSupplierIds) {
        this.prId = prId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = quantity * price;
        this.suggestedSupplierIds = (suggestedSupplierIds != null) ? new ArrayList<>(suggestedSupplierIds) : new ArrayList<>();
    }

    // Constructor used when total price is already calculated (e.g., from file)
    public PurchaseRequisitionItem(String prId, String itemId, String itemCode, String itemName, int quantity, int price, int totalPrice, List<String> suggestedSupplierIds) {
        this.prId = prId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
        this.suggestedSupplierIds = (suggestedSupplierIds != null) ? new ArrayList<>(suggestedSupplierIds) : new ArrayList<>();
    }

    // Existing constructor - adapt to initialize suggestedSupplierIds (e.g. for AddPurchaseRequisitionForm initial prefill)
    public PurchaseRequisitionItem(String prId, String itemId, String itemCode, String itemName, int quantity, int price) {
        this(prId, itemId, itemCode, itemName, quantity, price, new ArrayList<>()); // Initialize with empty list
    }


    // Getters
    public String getPrId() { return prId; }
    public String getItemId() { return itemId; }
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public int getPrice() { return price; }
    public int getTotalPrice() { return totalPrice; }
    public List<String> getSuggestedSupplierIds() {
        // Return a copy to prevent external modification
        return new ArrayList<>(suggestedSupplierIds != null ? suggestedSupplierIds : new ArrayList<>());
    }

    // Setters
    public void setPrId(String prId) { this.prId = prId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.price; // Recalculate total
    }
    public void setPrice(int price) {
        this.price = price;
        this.totalPrice = this.quantity * this.price; // Recalculate total
    }
    // No setter for totalPrice as it's derived
    public void setSuggestedSupplierIds(List<String> suggestedSupplierIds) {
        this.suggestedSupplierIds = (suggestedSupplierIds != null) ? new ArrayList<>(suggestedSupplierIds) : new ArrayList<>();
    }

    /**
     * Converts the item to a CSV string.
     * New format: prId,itemId,itemCode,itemName,quantity,price,totalPrice,suggestedSupplierIds_concatenated
     * SuggestedSupplierIds are joined by a semicolon.
     */
    public String toCSV() {
        String suppliersString = (suggestedSupplierIds != null && !suggestedSupplierIds.isEmpty()) ?
                String.join(";", suggestedSupplierIds) :
                ""; // Use empty string if no suppliers

        return String.join(",",
                prId != null ? prId.replace(",", ";") : "", // Escape commas in fields
                itemId != null ? itemId.replace(",", ";") : "",
                itemCode != null ? itemCode.replace(",", ";") : "",
                itemName != null ? itemName.replace(",", ";") : "",
                String.valueOf(quantity),
                String.valueOf(price),
                String.valueOf(totalPrice),
                suppliersString.replace(",", ";") // Ensure suppliersString itself doesn't have commas if somehow possible
        );
    }

    /**
     * Creates a PurchaseRequisitionItem from a CSV string.
     * Expected format: prId,itemId,itemCode,itemName,quantity,price,totalPrice,suggestedSupplierIds_concatenated
     */
    public static PurchaseRequisitionItem fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",", -1); // Use -1 to keep trailing empty strings

        // Original 7 fields + 1 new field for suppliers = 8 fields
        if (parts.length < 8) { // Expecting at least 8 parts now
            System.err.println("PurchaseRequisitionItem.fromCSV: Malformed line, expected at least 8 parts, got " + parts.length + ": " + csvLine);
            return null;
        }
        try {
            List<String> supplierIds = new ArrayList<>();
            String suppliersPart = parts[7].replace(";", ","); // Revert semicolon replacement if any during toCSV of suppliersString
            if (suppliersPart != null && !suppliersPart.trim().isEmpty()) {
                // Split by semicolon if that was the chosen delimiter in toCSV
                supplierIds.addAll(Arrays.asList(suppliersPart.split(";")));
                // Remove any empty strings that might result from splitting an empty suppliersPart or "N/A"
                supplierIds.removeIf(String::isEmpty);

            }

            return new PurchaseRequisitionItem(
                    parts[0].replace(";", ","), // prId
                    parts[1].replace(";", ","), // itemId
                    parts[2].replace(";", ","), // itemCode
                    parts[3].replace(";", ","), // itemName
                    Integer.parseInt(parts[4]), // quantity
                    Integer.parseInt(parts[5]), // price
                    Integer.parseInt(parts[6]), // totalPrice
                    supplierIds                 // parsed list of supplier IDs
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric fields for PurchaseRequisitionItem from CSV: " + csvLine + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing PurchaseRequisitionItem from CSV: " + csvLine + " - " + e.getMessage());
            return null;
        }
    }

    public String toJSON() { // Also update JSON if you use it elsewhere
        String suppliersJsonArray = "[]";
        if (suggestedSupplierIds != null && !suggestedSupplierIds.isEmpty()) {
            suppliersJsonArray = suggestedSupplierIds.stream()
                    .map(id -> "\"" + id + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
        }
        return "{" +
                "\"prId\": \"" + prId + "\"," +
                "\"itemId\": \"" + itemId + "\"," +
                "\"itemCode\": \"" + itemCode  + "\"," +
                "\"itemName\": \"" + itemName + "\"," +
                "\"quantity\": " + quantity + "\"," +
                "\"price\": " + price + "\"," +
                "\"totalPrice\": " + totalPrice + "\"," +
                "\"suggestedSupplierIds\": " + suppliersJsonArray +
                "}";
    }
}