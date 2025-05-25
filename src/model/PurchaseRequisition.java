package model;


import java.util.ArrayList;
import java.util.List;

public class PurchaseRequisition {
    private String prId, notes, supplierId, createdAt, createdBy, updatedAt, updatedBy;
    private int status;
    private List<PurchaseRequisitionItem> items;


    public PurchaseRequisition(String prId, String notes, String supplierId,
                               int status, String createdAt, String createdBy, String updatedAt, String updatedBy,
                               List<PurchaseRequisitionItem> itemsList) {
        this.prId = prId;
        this.notes = notes;
        this.supplierId = supplierId;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.items = itemsList != null ? new ArrayList<>(itemsList) : new ArrayList<>();
        for (PurchaseRequisitionItem item : this.items) {
            if (item.getPrId() == null || !item.getPrId().equals(this.prId)) {
                item.setPrId(this.prId);
            }
        }
    }

    // Constructor for creating header (items added via addItem or setItems)
    public PurchaseRequisition(String prId, String notes, String supplierId,
                               int status, String createdAt, String createdBy, String updatedAt, String updatedBy) {
        this(prId, notes, supplierId, status, createdAt, createdBy, updatedAt, updatedBy, new ArrayList<>());
    }

    // Getters
    public String getPrId() { return prId; }
    public String getNotes() { return notes; }
    public String getSupplierId() { return supplierId; } // Only supplierId
    public int getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public List<PurchaseRequisitionItem> getItems() { return items; }

    // Setters
    public void setPrId(String prId) { this.prId = prId; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; } // Only supplierId
    public void setStatus(int status) { this.status = status; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public void setItems(List<PurchaseRequisitionItem> items) {
        this.items = new ArrayList<>();
        if (items != null) {
            for (PurchaseRequisitionItem item : items) {
                this.addItem(item);
            }
        }
    }

    public void addItem(PurchaseRequisitionItem item) {
        if (item == null) return;
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        item.setPrId(this.prId);
        this.items.add(item);
    }

    /**
     * Generates CSV for the header part of the PR.
     * Supplier details (name, company, address) are NOT stored here, only supplierId.
     */
    public String toCSV() {
        // prId,notes,supplierId,status,createdAt,createdBy,updatedAt,updatedBy
        return String.join(",",
                prId != null ? prId.replace(",", ";") : "",
                notes != null ? notes.replace(",", ";") : "",
                supplierId != null ? supplierId.replace(",", ";") : "", // Only supplierId
                String.valueOf(status),
                createdAt != null ? createdAt : "",
                createdBy != null ? createdBy : "",
                updatedAt != null ? updatedAt : "",
                updatedBy != null ? updatedBy : ""
        );
    }


    public static PurchaseRequisition fromHeaderCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",");
        // prId,notes,supplierId,status,createdAt,createdBy,updatedAt,updatedBy
        if (parts.length < 8) {
            System.err.println("PurchaseRequisition.fromHeaderCSV: Malformed line, expected 8 parts, got " + parts.length + ": " + csvLine);
            return null;
        }
        try {
            return new PurchaseRequisition(
                    parts[0].replace(";", ","), // prId
                    parts[1].replace(";", ","), // notes
                    parts[2].replace(";", ","), // supplierId
                    Integer.parseInt(parts[3]), // status
                    parts[4], // createdAt
                    parts[5], // createdBy
                    parts[6], // updatedAt
                    parts[7]  // updatedBy
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing PR status from CSV: " + csvLine + " - " + e.getMessage());
            return null;
        }
    }

    public String toJSON() {
        StringBuilder itemsJson = new StringBuilder("[");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                itemsJson.append(items.get(i).toJSON()); // Assuming PurchaseRequisitionItem has toJSON
                if (i < items.size() - 1) {
                    itemsJson.append(",");
                }
            }
        }
        itemsJson.append("]");

        return "{" +
                "\"prId\": \"" + (prId != null ? prId.replace("\"", "\\\"") : "") + "\"," +
                "\"notes\": \"" + (notes != null ? notes.replace("\"", "\\\"") : "") + "\"," +
                "\"supplierId\": \"" + (supplierId != null ? supplierId.replace("\"", "\\\"") : "") + "\"," + // Only supplierId
                "\"status\": " + status + "," +
                "\"createdAt\": \"" + (createdAt != null ? createdAt : "") + "\"," +
                "\"createdBy\": \"" + (createdBy != null ? createdBy : "") + "\"," +
                "\"updatedAt\": \"" + (updatedAt != null ? updatedAt : "") + "\"," +
                "\"updatedBy\": \"" + (updatedBy != null ? updatedBy : "") + "\"," +
                "\"items\": " + itemsJson.toString() +
                "}";
    }
}
