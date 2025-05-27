package controller;

import model.EntityType;
import model.Item;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void addItem(String itemCode, String itemName, String unit, String unitPrice, String supplierId) {
        try {
            String[] existingItemDetails = fileController.getLine(1, itemCode);
            if (existingItemDetails == null) {
                int tempItemId = FileController.getFile().size()+1;
                String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String createdBy = SessionController.getInstance().getUserId();
                String updatedBy = SessionController.getInstance().getUserId();
                Item item = new Item("IM" + String.format("%03d", tempItemId), itemCode, itemName, unit, unitPrice, supplierId, createdAt, createdAt, createdBy, updatedBy);
                add(item);
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

    public void updateItem(String itemEntryId, String itemCode, String itemName, String unit, String unitPrice, String supplierId) {
        try {
            String[] existingItemDetails = fileController.getLine(0, itemEntryId);
            if (existingItemDetails == null) {
                System.out.println("Item not found");
                return;
            }
            Item item = new Item(itemEntryId, existingItemDetails[1], existingItemDetails[2], existingItemDetails[3], existingItemDetails[4], existingItemDetails[5], existingItemDetails[6], existingItemDetails[7], existingItemDetails[8], existingItemDetails[9]);
            item.setItemCode((itemCode != null) ? itemCode : existingItemDetails[1]);
            item.setItemName((itemName != null) ? itemName : existingItemDetails[2]);
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

    public String getOneByItemCode(String itemCode) {
        try {
            String[] details = fileController.getLine(1, itemCode);
            if (details != null) {
                return String.join(",", details);
            } else {
                System.out.println("Item not found");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error reading the item from file: " + e.getMessage());
            return null;
        }
    }

    public String getOneByItemName(String itemName) {
        try {
            String[] details = fileController.getLine(2, itemName);
            if (details != null) {
                return String.join(",", details);
            } else {
                System.err.println("Item not found");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error reading the item from file: " + e.getMessage());
            return null;
        }
    }

}
