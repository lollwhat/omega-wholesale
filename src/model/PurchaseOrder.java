package model;


public class PurchaseOrder {
    private String poId;
    private String poDate;           // Added for PO date
    private String prId;
    private String itemName;
    private int quantity;
    private String supplierName;
    private String status;           // Changed to String since your examples are "Pending", "Approved"
    private String createdBy;
    private String approvedBy;       // Added approvedBy

    // Constructor
    public PurchaseOrder(String poId, String poDate, String prId, String itemName, int quantity, String supplierName, String status, String createdBy, String approvedBy) {
        this.poId = poId;
        this.poDate = poDate;
        this.prId = prId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.supplierName = supplierName;
        this.status = status;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
    }

    // Getters and Setters
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }

    public String getPoDate() { return poDate; }
    public void setPoDate(String poDate) { this.poDate = poDate; }

    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    // toCSV for file saving, matching your table fields
    public String toCSV() {
        return poId + "," + poDate + "," + prId + "," + itemName + "," + quantity + "," + supplierName + "," + status + "," + createdBy + "," + approvedBy;
    }
}
