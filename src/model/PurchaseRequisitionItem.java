package model;

public class PurchaseRequisitionItem {
    private String prId;
    private String itemId;
    private String itemCode;
    private String itemName;
    private int quantity;
    private int price;
    private int totalPrice;

    public PurchaseRequisitionItem(String prId, String itemId, String itemCode, String itemName, int quantity, int price) {
        this.prId = prId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = quantity * price;
    }

    public PurchaseRequisitionItem(String prId, String itemId, String itemCode, String itemName, int quantity, int price, int totalPrice) {
        this.prId = prId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }

    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.price;
    }

    public int getPrice() { return price; }
    public void setPrice(int price) {
        this.price = price;
        this.totalPrice = this.quantity * this.price;
    }

    public int getTotalPrice() { return totalPrice; }

    public String toCSV() {
        // prId,itemId,itemCode,itemName,quantity,price,totalPrice
        return String.join(",",
                prId != null ? prId.replace(",", ";") : "",
                itemId != null ? itemId.replace(",", ";") : "",
                itemCode != null ? itemCode.replace(",", ";") : "",
                itemName != null ? itemName.replace(",", ";") : "",
                String.valueOf(quantity),
                String.valueOf(price),
                String.valueOf(totalPrice)
        );
    }

    public static PurchaseRequisitionItem fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",");
        // prId,itemId,itemCode,itemName,quantity,price,totalPrice
        if (parts.length < 7) {
            System.err.println("PurchaseRequisitionItem.fromCSV: Malformed line, expected 7 parts, got " + parts.length + ": " + csvLine);
            return null;
        }
        try {
            return new PurchaseRequisitionItem(
                    parts[0].replace(";", ","), // prId
                    parts[1].replace(";", ","), // itemId
                    parts[2].replace(";", ","), // itemCode
                    parts[3].replace(";", ","), // itemName
                    Integer.parseInt(parts[4]), // quantity
                    Integer.parseInt(parts[5]), // price
                    Integer.parseInt(parts[6])  // totalPrice
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing PurchaseRequisitionItem from CSV: " + csvLine + " - " + e.getMessage());
            return null;
        }
    }

    public String toJSON() {
        return "{" +
                "\"prId\": \"" + prId + "\"," +
                "\"itemId\": \"" + itemId + "\"," +
                "\"itemCode\": \"" + itemCode + "\"," +
                "\"itemName\": \"" + itemName + "\"," +
                "\"quantity\": " + quantity + "," +
                "\"price\": " + price + "," +
                "\"totalPrice\": " + totalPrice +
                "}";
    }
}
