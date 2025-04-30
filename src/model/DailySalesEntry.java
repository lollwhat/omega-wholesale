package model;


public class DailySalesEntry {
    private String dailySalesId, itemId, itemName, salesDate, createdAt, createdBy, updatedAt, updatedBy;
    private int quantity, totalPrice;

    public DailySalesEntry(String dailySalesId, String itemId, String itemName, String salesDate, int quantity, int totalPrice, String createdAt, String createdBy, String updatedAt, String updatedBy) {
        this.dailySalesId = dailySalesId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.salesDate = salesDate;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getDailySalesId(){ return dailySalesId; }
    public void setDailySalesId(String dailySalesId){ this.dailySalesId = dailySalesId; }

    public String getItemId(){ return itemId; }
    public void setItemId(String itemId){ this.itemId = itemId; }

    public String getItemName(){ return itemName; }
    public void setItemName(String itemName){ this.itemName = itemName; }

    public String getSalesDate(){ return salesDate; }
    public void setSalesDate(String salesDate){ this.salesDate = salesDate; }

    public int getQuantity(){ return quantity; }
    public void setQuantity(int quantity){ this.quantity = quantity; }

    public int getTotalPrice(){ return totalPrice; }
    public void setTotalPrice(int totalPrice){ this.totalPrice = totalPrice; }

    public String getCreatedAt(){ return createdAt; }
    public void setCreatedAt(String createdAt){ this.createdAt = createdAt; }

    public String getCreatedBy(){ return createdBy; }
    public void setCreatedBy(String createdBy){ this.createdBy = createdBy; }

    public String getUpdatedAt(){ return updatedAt; }
    public void setUpdatedAt(String updatedAt){ this.updatedAt = updatedAt; }

    public String getUpdatedBy(){ return updatedBy; }
    public void setUpdatedBy(String updatedBy){ this.updatedBy = updatedBy; }

    public String toCSV() {
        return dailySalesId + "," + itemId + "," + itemName  + "," + salesDate + "," + quantity + "," + totalPrice + "," + createdAt + "," + createdBy + "," + updatedAt + "," + updatedBy;
    }

    public String toJSON() {
        return "{ \"dailySalesId\": \"" + dailySalesId + "\", \"itemId\": \"" + itemId + "\", \"itemName\": \"" + itemName + "\", \"salesDate\": \"" + salesDate + "\", \"quantity\": " + quantity + "\", \"totalPrice\": " + totalPrice + ", \"createdAt\": \"" + createdAt + "\", \"createdBy\": \"" + createdBy + "\", \"updatedAt\": \"" + updatedAt + "\", \"updatedBy\": \"" + updatedBy + "\" }";
    }
}
