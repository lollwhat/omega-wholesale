// Path: omega-wholesale/src/model/PurchaseRequisition.java
package model;

import java.util.ArrayList;
import java.util.List;

public class PurchaseRequisition {
    private String prId;
    private String notes;
    private String createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;
    private int status;
    private List<PurchaseRequisitionItem> items;

    public PurchaseRequisition(String prId, String notes,
                               int status, String createdAt, String createdBy, String updatedAt, String updatedBy,
                               List<PurchaseRequisitionItem> itemsList) {
        this.prId = prId;
        this.notes = notes;
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

    // Constructor for creating header (items added via addItem or setItems) (supplierId removed)
    public PurchaseRequisition(String prId, String notes,
                               int status, String createdAt, String createdBy, String updatedAt, String updatedBy) {
        this(prId, notes, status, createdAt, createdBy, updatedAt, updatedBy, new ArrayList<>());
    }

    // Getters
    public String getPrId() { return prId; }
    public String getNotes() { return notes; }
    public int getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public List<PurchaseRequisitionItem> getItems() { return new ArrayList<>(items != null ? items : new ArrayList<>()); }

    // Setters
    public void setPrId(String prId) { this.prId = prId; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(int status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
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
        item.setPrId(this.prId); // Ensure item's PR ID is set
        this.items.add(item);
    }

    /**
     * Generates CSV for the header part of the PR.
     * New format: prId,notes,status,createdAt,createdBy,updatedAt,updatedBy
     * (supplierId column removed)
     */
    public String toCSV() {
        return String.join(",",
                prId != null ? prId.replace(",", ";") : "",
                notes != null ? notes.replace(",", ";") : "",
                // supplierId != null ? supplierId.replace(",", ";") : "", // REMOVED
                String.valueOf(status),
                createdAt != null ? createdAt : "",
                createdBy != null ? createdBy : "",
                updatedAt != null ? updatedAt : "",
                updatedBy != null ? updatedBy : ""
        );
    }

    /**
     * Creates a PurchaseRequisition header from a CSV string.
     * Expected format: prId,notes,status,createdAt,createdBy,updatedAt,updatedBy
     * (supplierId column removed, now 7 parts)
     */
    public static PurchaseRequisition fromHeaderCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",", -1);

        if (parts.length < 7) { // Expecting 7 parts now
            System.err.println("PurchaseRequisition.fromHeaderCSV: Malformed line, expected 7 parts, got " + parts.length + ": " + csvLine);
            return null;
        }
        try {
            return new PurchaseRequisition(
                    parts[0].replace(";", ","), // prId
                    parts[1].replace(";", ","), // notes
                    // parts[2] was supplierId, REMOVED
                    Integer.parseInt(parts[2]),   // status (was parts[3])
                    parts[3],                     // createdAt (was parts[4])
                    parts[4],                     // createdBy (was parts[5])
                    parts[5],                     // updatedAt (was parts[6])
                    parts[6]                      // updatedBy (was parts[7])
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing PR status from CSV: " + csvLine + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing PR fromHeaderCSV: " + csvLine + " - " + e.getMessage());
            return null;
        }
    }

    public String toJSON() { // Also update JSON
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
                "\"prId\": \"" + prId + "\"," +
                "\"notes\": \"" + notes + "\"," +
                "\"status\": " + status + "," +
                "\"createdAt\": \"" + createdAt + "\"," +
                "\"createdBy\": \"" + createdBy + "\"," +
                "\"updatedAt\": \"" + updatedAt + "\"," +
                "\"updatedBy\": \"" + updatedBy + "\"," +
                "\"items\": " + itemsJson.toString() +
                "}";
    }
}