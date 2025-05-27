package model;

public class PurchaseOrderItem {
    private String poId;
    private String itemId;
    private String itemCode;
    private String itemName;
    private int quantity;
    private int price;
    private int totalPrice; // auto-calculated: quantity * price

    public PurchaseOrderItem(String poId, String itemId, String itemCode, String itemName, int quantity, int price) {
        this.poId = poId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = quantity * price;
    }

    public PurchaseOrderItem(String poId, String itemId, String itemCode, String itemName, int quantity, int price, int totalPrice) {
        this.poId = poId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }

    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }

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
        // poId,itemId,itemCode,itemName,quantity,price,totalPrice
        return String.join(",",
                poId != null ? poId.replace(",", ";") : "",
                itemId != null ? itemId.replace(",", ";") : "",
                itemCode != null ? itemCode.replace(",", ";") : "",
                itemName != null ? itemName.replace(",", ";") : "",
                String.valueOf(quantity),
                String.valueOf(price),
                String.valueOf(totalPrice)
        );
    }

    public static PurchaseOrderItem fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",");
        if (parts.length < 7) {
            System.err.println("PurchaseOrderItem.fromCSV: Malformed line, expected 7 parts, got " + parts.length + ": " + csvLine);
            return null;
        }
        try {
            return new PurchaseOrderItem(
                    parts[0].replace(";", ","), // poId
                    parts[1].replace(";", ","), // itemId
                    parts[2].replace(";", ","), // itemCode
                    parts[3].replace(";", ","), // itemName
                    Integer.parseInt(parts[4]), // quantity
                    Integer.parseInt(parts[5]), // price
                    Integer.parseInt(parts[6])  // totalPrice
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing PurchaseOrderItem from CSV: " + csvLine + " - " + e.getMessage());
            return null;
        }
    }

    public String toJSON() {
        return "{" +
                "\"poId\": \"" + poId + "," +
                "\"itemId\": \"" + itemId + "," +
                "\"itemCode\": \"" + itemCode + "," +
                "\"itemName\": \"" + itemName + "," +
                "\"quantity\": " + quantity + "," +
                "\"price\": " + price + "," +
                "\"totalPrice\": " + totalPrice +
                "}";
    }
}