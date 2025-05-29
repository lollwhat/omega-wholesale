package model;

public class ItemSupplier {
    private String itemId;
    private String supplierId;

    public ItemSupplier(String itemId, String supplierId) {
        this.itemId = itemId;
        this.supplierId = supplierId;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String toCSV() {
        return String.join(",",
                itemId != null ? itemId.replace(",", ";") : "",
                supplierId != null ? supplierId.replace(",", ";") : ""
        );
    }

    public static ItemSupplier fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",");
        if (parts.length < 2) {
            System.err.println("ItemSupplier.fromCSV: Malformed line, expected 2 parts, got " + parts.length + ": " + csvLine);
            return null;
        }

        return new ItemSupplier(
                parts[0].replace(";", ","),
                parts[1].replace(";", ",")
        );
    }

    public String toJSON() {
        return "{" +
                "\"itemId\": \"" + (itemId != null ? itemId.replace("\"", "\\\"") : "") + "\"," +
                "\"supplierId\": \"" + (supplierId != null ? supplierId.replace("\"", "\\\"") : "") + "\"" +
                "}";
    }
}