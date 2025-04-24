package controller;

import model.EntityType;
import model.Item;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

public class ItemController extends CRUDController<Item> {
    public ItemController() {
        super("data/item_details.txt", EntityType.ITEM);
    }

//    public List<String> getAllItems() {
//        try {
//            List<String> lines = FileController.getFile();
//            if (lines == null || lines.isEmpty()) {
//                System.out.println("No items found");
//                return null;
//            }
//            return lines;
//        } catch (Exception e) {
//            System.out.println("Error reading all items from file: " + e.getMessage());
//            return null;
//        }
//    }
//
//    public String getOneItemWithId(String itemId) {
//        try {
//            String[] itemDetails = fileController.getLine(0, itemId);
//            if (itemDetails != null) {
//                System.out.println("Item found: " + String.join(",", itemDetails));
//                return String.join(",", itemDetails);
//            } else {
//                System.out.println("Item not found");
//                return null;
//            }
//        } catch (Exception e) {
//            System.out.println("Error reading the item from file: " + e.getMessage());
//            return null;
//        }
//    }

    @Override
    public void add(Item item) {
        try {
            String data = item.toCSV();
            FileController.appendFile(data);
            System.out.println("Item added successfully");
        } catch (Exception e) {
            System.out.println("Error adding item to file: " + e.getMessage());
        }
    }

    public void addItem(String itemName, int quantity, String unit, String unitPrice, String supplierId) {
        int tempItemId = FileController.getFile().size()+1;
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();
        String updatedBy = SessionController.getInstance().getUserId();
        Item item = new Item("IM" + String.format("%03d", tempItemId), itemName, quantity, unit, unitPrice, supplierId, createdAt, createdAt, createdBy, updatedBy);
        add(item);
        String data = item.toCSV();
        FileController.appendFile(data);
        System.out.println("Item added successfully");
    }

    @Override
    public void update(Item item) {
        try {
            String data = item.toCSV();
            fileController.updateFile(data);
            System.out.println("Item updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating item in file: " + e.getMessage());
        }
    }

    public void updateItem(String itemId, String itemName, int quantity, String unit, String unitPrice, String supplierId) {
        try {
            String[] existingItemDetails = fileController.getLine(0, itemId);
            if (existingItemDetails == null) {
                System.out.println("Item not found");
                return;
            }
            Item item = new Item(itemId, existingItemDetails[1], Integer.parseInt(existingItemDetails[2]), existingItemDetails[3], existingItemDetails[4], existingItemDetails[5], existingItemDetails[6], existingItemDetails[7], existingItemDetails[8], existingItemDetails[9]);
            item.setItemName((itemName != null) ? itemName : existingItemDetails[1]);
            item.setQuantity((quantity != 0) ? quantity : Integer.parseInt(existingItemDetails[2]));
            item.setUnit((unit != null) ? unit : existingItemDetails[3]);
            item.setUnitPrice((unitPrice != null) ? unitPrice : existingItemDetails[4]);
            item.setSupplierId((supplierId != null) ? supplierId : existingItemDetails[5]);
            item.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            item.setUpdatedBy(SessionController.getInstance().getUserId());
            update(item);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

//    public void deleteItem(String itemId) {
//        try {
//            fileController.deleteLine(itemId, 0);
//            System.out.println("Item deleted successfully");
//        } catch (Exception e) {
//            System.out.println("Error deleting item from file: " + e.getMessage());
//        }
//    }
}
