package model;

import controller.FileController;

public class Item {
    private String itemEntryId;
    private String itemCode;
    private String itemName;
    private String unit;
    private String unitPrice;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;

    public Item(String itemEntryId, String itemCode, String itemName, String unit, String unitPrice, String createdAt, String updatedAt, String createdBy, String updatedBy) {
        this.itemEntryId = itemEntryId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public String getItemId() {
        return itemEntryId;
    }
    public void setItemId(String itemEntryId) {
        this.itemEntryId = itemEntryId;
    }

    public String getItemCode() {
        return itemCode;
    }
    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) { this.unit = unit; }

    public String getUnitPrice() { return unitPrice; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String toCSV() {
        return String.join(",", itemEntryId, itemCode, itemName, unit, unitPrice, createdAt, updatedAt, createdBy, updatedBy);
    }

    public String toJSON() {
        return "{" +
                "\"itemEntryId\": \"" + itemEntryId + "\"," +
                "\"itemCode\": \"" + itemCode + "\"," +
                "\"itemName\": \"" + itemName + "\"," +
                "\"unit\": \"" + unit + "\"," +
                "\"unitPrice\": \"" + unitPrice + "\"," +
                "\"createdAt\": \"" + createdAt + "\"," +
                "\"updatedAt\": \"" + updatedAt + "\"," +
                "\"createdBy\": \"" + createdBy + "\"," +
                "\"updatedBy\": \"" + updatedBy + "\"" +
                "}";
    }

}
