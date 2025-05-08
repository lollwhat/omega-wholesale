package model;


public class PurchaseOrder {
    private String poId, prId, itemId, itemName, notes, supplierId, supplierName, supplierCompanyName, supplierAddress, createdAt, createdBy, updatedAt, updatedBy, receivedAt, receivedBy;
    private int price, quantity, totalPrice, status;


    public PurchaseOrder(String poId, String prId, String itemId, String itemName, String notes, int price, int quantity, int totalPrice, String supplierId, String supplierName, String supplierCompanyName, String supplierAddress, int status, String createdAt, String createdBy, String updatedAt, String updatedBy, String receivedAt, String receivedBy) {
        this.poId = poId;
        this.prId = prId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.notes = notes;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierCompanyName = supplierCompanyName;
        this.supplierAddress = supplierAddress;
        this.status = status; // temporary set like this -> 0: processing, 1: received, 2: cancelled
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.receivedAt = receivedAt==null ? "processing" : receivedAt;
        this.receivedBy = receivedBy==null ? "processing" : receivedBy;
    }

    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }

    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierCompanyName() { return supplierCompanyName; }
    public void setSupplierCompanyName(String supplierCompanyName) { this.supplierCompanyName = supplierCompanyName; }

    public String getSupplierAddress() { return supplierAddress; }
    public void setSupplierAddress(String supplierAddress) { this.supplierAddress = supplierAddress; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getReceivedAt() { return receivedAt; }
    public void setReceivedAt(String receivedAt) { this.receivedAt = receivedAt; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }


    public String toCSV() {
        return prId + ',' + itemId + ',' + itemName + ',' + notes + ',' + price + ',' + quantity + ',' + totalPrice + ',' + supplierId + ',' + supplierName + ',' + supplierCompanyName + ',' + supplierAddress + ',' + status + ',' + createdAt + ',' + createdBy + ',' + updatedAt + ',' + updatedBy + ',' + receivedAt + ',' + receivedBy;
    }

    public String toJSON() {
        return "{ " +
                "\"prId\": \"" + prId + "\", " +
                "\"itemId\": \"" + itemId + "\", " +
                "\"itemName\": \"" + itemName + "\", " +
                "\"notes\": \"" + notes + "\", " +
                "\"price\": " + price + ", " +
                "\"quantity\": " + quantity + ", " +
                "\"totalPrice\": " + totalPrice + ", " +
                "\"supplierId\": \"" + supplierId + "\", " +
                "\"supplierName\": \"" + supplierName + "\", " +
                "\"supplierCompanyName\": \"" + supplierCompanyName + "\", " +
                "\"supplierAddress\": \"" + supplierAddress + "\", " +
                "\"status\": \"" + status + "\", " +
                "\"createdAt\": \"" + createdAt + "\", " +
                "\"createdBy\": \"" + createdBy + "\", " +
                "\"updatedAt\": \"" + updatedAt + "\", " +
                "\"updatedBy\": \"" + updatedBy + "\" " +
                "\"receivedAt\": \"" + receivedAt + "\", " +
                "\"receivedBy\": \"" + receivedBy + "\" " +
                "}";
    }
}
