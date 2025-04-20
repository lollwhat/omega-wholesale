package controller;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

public class ItemController {
    private String itemId;
    private String itemName;
    private int quantity;
    private String supplierId;
    private String unit;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
    private String itemDetailsFile = "data/item_details.txt";
    private FileController fileController = new FileController(itemDetailsFile);

    public ItemController() {
        // Default constructor
    }

    public List<String> getAllItems() {
        try {
            List<String> lines = fileController.getFile();
            if (lines == null || lines.isEmpty()) {
                System.out.println("No items found");
                return null;
            }
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOneItemWithId(String itemId) {
        try {
            String[] itemDetails = fileController.getLine(0, itemId);
            if (itemDetails != null) {
                System.out.println("Item found: " + String.join(",", itemDetails));
                return String.join(",", itemDetails);
            } else {
                System.out.println("Item not found");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addItem(String itemName, int quantity, String unit, String supplierId) {
        try {
            int tempItemId = fileController.getFile().size()+1;
            this.itemId = "IM" + String.format("%03d", tempItemId);
            this.itemName = itemName;
            this.quantity = quantity;
            this.unit = unit;
            this.supplierId = supplierId;
            this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.updatedAt = createdAt;
            this.createdBy = SessionController.getInstance().getUserId();
            this.updatedBy = SessionController.getInstance().getUserId();
            String data = String.join(",", itemId, itemName, String.valueOf(quantity), unit, supplierId, createdAt, updatedAt, createdBy, updatedBy);
            fileController.appendFile(data);
            System.out.println("Item added successfully");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to add item: " + e.getMessage());
        }
    }

    public void updateItem(String itemId, String itemName, int quantity, String unit, String supplierId) {
        try {
            String[] existingItemDetails = fileController.getLine(0, itemId);
            if (existingItemDetails == null) {
                System.out.println("Item not found");
                return;
            }
            this.itemId = itemId;
            this.itemName = (itemName != null) ? itemName : existingItemDetails[1];
            this.quantity = (quantity != 0) ? quantity : Integer.parseInt(existingItemDetails[2]);
            this.unit = (unit != null) ? unit : existingItemDetails[3];
            this.supplierId = (supplierId != null) ? supplierId : existingItemDetails[4];
            this.createdAt = existingItemDetails[5];
            this.updatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.createdBy = existingItemDetails[7];
            this.updatedBy = SessionController.getInstance().getUserId();

            String data = String.join(",", this.itemId, this.itemName, String.valueOf(this.quantity), this.unit, this.supplierId, createdAt, updatedAt, createdBy, updatedBy);
            fileController.updateFile(data);
            System.out.println("Item updated successfully");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to update item: " + e.getMessage());
        }
    }

    public void deleteItem(String itemId) {
        try {
            fileController.deleteLine(itemId, 0);
            System.out.println("Item deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to delete item: " + e.getMessage());
        }
    }
}
