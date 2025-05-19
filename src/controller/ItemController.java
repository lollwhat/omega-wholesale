package controller;

import model.EntityType;
import model.Item;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ItemController extends CRUDController<Item> {
    public ItemController() {
        super("data/item_details.txt", EntityType.ITEM);
    }

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

    public void addItem(String itemCode, String itemName, int quantity, String unit, String unitPrice, String supplierId) {
        try {
            String[] existingItemDetails = fileController.getLine(1, itemCode);
            if (existingItemDetails != null) {
                int tempItemId = FileController.getFile().size()+1;
                String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String createdBy = SessionController.getInstance().getUserId();
                String updatedBy = SessionController.getInstance().getUserId();
                Item item = new Item("IM" + String.format("%03d", tempItemId), itemCode, itemName, quantity, unit, unitPrice, supplierId, createdAt, createdAt, createdBy, updatedBy);
                add(item);
                String data = item.toCSV();
                FileController.appendFile(data);
                System.out.println("Item added successfully");
            } else {
                System.out.println("Item with code: " + itemCode + " already exists. Please update the item instead.");
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }


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

    public void updateItem(String itemEntryId, String itemCode, String itemName, int quantity, String unit, String unitPrice, String supplierId) {
        try {
            String[] existingItemDetails = fileController.getLine(0, itemEntryId);
            if (existingItemDetails == null) {
                System.out.println("Item not found");
                return;
            }
            Item item = new Item(itemEntryId, existingItemDetails[1], existingItemDetails[2], Integer.parseInt(existingItemDetails[3]), existingItemDetails[4], existingItemDetails[5], existingItemDetails[6], existingItemDetails[7], existingItemDetails[8], existingItemDetails[9], existingItemDetails[10]);
            item.setItemCode((itemCode != null) ? itemCode : existingItemDetails[1]);
            item.setItemName((itemName != null) ? itemName : existingItemDetails[2]);
            item.setQuantity((quantity != 0) ? quantity : Integer.parseInt(existingItemDetails[3]));
            item.setUnit((unit != null) ? unit : existingItemDetails[4]);
            item.setUnitPrice((unitPrice != null) ? unitPrice : existingItemDetails[5]);
            item.setSupplierId((supplierId != null) ? supplierId : existingItemDetails[6]);
            item.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            item.setUpdatedBy(SessionController.getInstance().getUserId());
            update(item);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
