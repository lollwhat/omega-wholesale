package model;

import controller.FileController;

public class Item {
    private String itemId;
    private String itemName;
    private int quantity;
    private String supplierId;
    private String unit;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;

    public Item(String itemId, String itemName, int quantity, String supplierId, String unit, String createdAt, String updatedAt, String createdBy, String updatedBy) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.supplierId = supplierId;
        this.unit = unit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String toCSV() {
        return String.join(",", itemId, itemName, String.valueOf(quantity), supplierId, unit, createdAt, updatedAt, createdBy, updatedBy);
    }

    public String toJSON() {
        return "{" +
                "\"itemId\": \"" + itemId + "\"," +
                "\"itemName\": \"" + itemName + "\"," +
                "\"quantity\": " + quantity + "," +
                "\"unit\": \"" + unit + "\"," +
                "\"supplierId\": \"" + supplierId + "\"," +
                "\"createdAt\": \"" + createdAt + "\"," +
                "\"updatedAt\": \"" + updatedAt + "\"," +
                "\"createdBy\": \"" + createdBy + "\"," +
                "\"updatedBy\": \"" + updatedBy + "\"" +
                "}";
    }

}
