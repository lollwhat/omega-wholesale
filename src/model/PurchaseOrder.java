package model;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder {
    private String poId;
    private String prId;
    private String notes;
    private int status; // 0: pending, 1: received, 2: cancelled
    private String createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;
    private String receivedAt;
    private String receivedBy;

    private List<PurchaseOrderItem> items;

    public PurchaseOrder(String poId, String prId, String notes,
                         int status, String createdAt, String createdBy, String updatedAt, String updatedBy,
                         String receivedAt, String receivedBy, List<PurchaseOrderItem> itemsList) {
        this.poId = poId;
        this.prId = prId;
        this.notes = notes;
        this.status = status; // 0: Pending, 1: Approved, 2: Received, 3: Cancelled
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.receivedAt = (receivedAt == null || receivedAt.equalsIgnoreCase("Pending")) ? null : receivedAt;
        this.receivedBy = (receivedBy == null || receivedBy.equalsIgnoreCase("Pending")) ? null : receivedBy;
        this.items = itemsList != null ? new ArrayList<>(itemsList) : new ArrayList<>();
        for (PurchaseOrderItem item : this.items) {
            if (item.getPoId() == null || !item.getPoId().equals(this.poId)) {
                item.setPoId(this.poId);
            }
        }
    }

    public PurchaseOrder(String poId, String prId, String notes,
                         int status, String createdAt, String createdBy, String updatedAt, String updatedBy,
                         String receivedAt, String receivedBy) {
        this(poId, prId, notes, status, createdAt, createdBy, updatedAt, updatedBy, receivedAt, receivedBy, new ArrayList<>());
    }

    // Getters
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }

    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

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
    public void setReceivedAt(String receivedAt) { this.receivedAt = (receivedAt != null && receivedAt.equalsIgnoreCase("Pending")) ? null : receivedAt; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = (receivedBy != null && receivedBy.equalsIgnoreCase("Pending")) ? null : receivedBy; }

    public List<PurchaseOrderItem> getItems() { return items; }


    public void setItems(List<PurchaseOrderItem> items) {
        this.items = new ArrayList<>();
        if (items != null) {
            for (PurchaseOrderItem item : items) {
                this.addItem(item);
            }
        }
    }

    public void addItem(PurchaseOrderItem item) {
        if (item == null) return;
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        item.setPoId(this.poId);
        this.items.add(item);
    }

    public String toCSV() {
        // poId,prId,notes,status,createdAt,createdBy,updatedAt,updatedBy,receivedAt,receivedBy
        return String.join(",",
                poId != null ? poId.replace(",", ";") : "",
                prId != null ? prId.replace(",", ";") : "",
                notes != null ? notes.replace(",", ";") : "",
                String.valueOf(status),
                createdAt != null ? createdAt : "",
                createdBy != null ? createdBy : "",
                updatedAt != null ? updatedAt : "",
                updatedBy != null ? updatedBy : "",
                receivedAt != null ? receivedAt : "",
                receivedBy != null ? receivedBy : ""
        );
    }

    public static PurchaseOrder fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",");
        // poId,prId,notes,status,createdAt,createdBy,updatedAt,updatedBy,receivedAt,receivedBy
        if (parts.length < 10) {
            System.err.println("PurchaseOrder.fromHeaderCSV: Malformed line, expected 10 parts, got " + parts.length + ": " + csvLine);
            return null;
        }
        try {
            return new PurchaseOrder(
                    parts[0].replace(";", ","), // poId
                    parts[1].replace(";", ","), // prId
                    parts[2].replace(";", ","), // notes
                    Integer.parseInt(parts[3]), // status
                    parts[4], // createdAt
                    parts[5], // createdBy
                    parts[6], // updatedAt
                    parts[7], // updatedBy
                    parts[8].isEmpty() ? null : parts[8],  // receivedAt - nullable
                    parts[9].isEmpty() ? null : parts[9] // receivedBy - nullable
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing PO status from CSV: " + csvLine + " - " + e.getMessage());
            return null;
        }
    }

    public String toJSON() {
        StringBuilder itemsJson = new StringBuilder("[");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                itemsJson.append(items.get(i).toJSON());
                if (i < items.size() - 1) {
                    itemsJson.append(",");
                }
            }
        }
        itemsJson.append("]");

        return "{" +
                "\"poId\": \"" + poId + "\"," +
                "\"prId\": \"" + prId + "\"," +
                "\"notes\": \"" + notes  + "\"," +
                "\"status\": " + status + "\"," +
                "\"createdAt\": \"" + createdAt + "\"," +
                "\"createdBy\": \"" + createdBy  + "\"," +
                "\"updatedAt\": \"" + updatedAt + "\"," +
                "\"updatedBy\": \"" + updatedBy + "\"," +
                "\"receivedAt\": \"" + receivedAt + "\"," +
                "\"receivedBy\": \"" + receivedBy + "\"," +
                "\"items\": " + itemsJson.toString() +
                "}";
    }
}