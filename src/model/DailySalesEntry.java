package model;


public class DailySalesEntry {
    private String dailySalesId, itemCode, itemName, salesDate, createdAt, createdBy, updatedAt, updatedBy;
    private int quantity;
    private double netIncome;

    public DailySalesEntry(String dailySalesId, String itemCode, String itemName, String salesDate, int quantity, double netIncome, String createdAt, String createdBy, String updatedAt, String updatedBy) {
        this.dailySalesId = dailySalesId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.salesDate = salesDate;
        this.quantity = quantity;
        this.netIncome = netIncome; // Net Income
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getDailySalesId(){ return dailySalesId; }
    public void setDailySalesId(String dailySalesId){ this.dailySalesId = dailySalesId; }

    public String getItemCode(){ return itemCode; }
    public void setItemCode(String itemCode){ this.itemCode = itemCode; }

    public String getItemName(){ return itemName; }
    public void setItemName(String itemName){ this.itemName = itemName; }

    public String getSalesDate(){ return salesDate; }
    public void setSalesDate(String salesDate){ this.salesDate = salesDate; }

    public int getQuantity(){ return quantity; }
    public void setQuantity(int quantity){ this.quantity = quantity; }

    public double getNetIncome(){ return netIncome; }
    public void setNetIncome(double netIncome){ this.netIncome = netIncome; }

    public String getCreatedAt(){ return createdAt; }
    public void setCreatedAt(String createdAt){ this.createdAt = createdAt; }

    public String getCreatedBy(){ return createdBy; }
    public void setCreatedBy(String createdBy){ this.createdBy = createdBy; }

    public String getUpdatedAt(){ return updatedAt; }
    public void setUpdatedAt(String updatedAt){ this.updatedAt = updatedAt; }

    public String getUpdatedBy(){ return updatedBy; }
    public void setUpdatedBy(String updatedBy){ this.updatedBy = updatedBy; }

    public String toCSV() {
        return dailySalesId + "," + itemCode + "," + itemName  + "," + salesDate + "," + quantity + "," + netIncome + "," + createdAt + "," + createdBy + "," + updatedAt + "," + updatedBy;
    }

    public String toJSON() {
        return "{ \"dailySalesId\": \"" + dailySalesId + "\", \"itemCode\": \"" + itemCode + "\", \"itemName\": \"" + itemName + "\", \"salesDate\": \"" + salesDate + "\", \"quantity\": " + quantity + "\", \"netIncome\": " + netIncome + ", \"createdAt\": \"" + createdAt + "\", \"createdBy\": \"" + createdBy + "\", \"updatedAt\": \"" + updatedAt + "\", \"updatedBy\": \"" + updatedBy + "\" }";
    }
}
