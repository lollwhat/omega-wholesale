package controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

public class ItemController {
    private String itemId;
    private String itemName;
    private int quantity;
    private String supplierId;
    private String unit;
    private String createdAt;
    private String updatedAt;
    private String createBy;
    private String updatedBy;
    private String itemDetailsFile = "data/item_details.txt";

    public ItemController(String itemId, String itemName, int quantity, String unit, String supplierId){
        // Constructor
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.supplierId = supplierId;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.updatedAt = createdAt;
    }

    public String getAllItems() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(itemDetailsFile));
            ArrayList<String> items = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                items.add(line);
            }
            reader.close();
            return items.toString();
        } catch (Exception e) {
            return "Error occurs: " + e.getMessage();
        }
    }

    public void addItem() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(itemDetailsFile, true));
            writer.write(itemId + "," + itemName + "," + quantity + "," + supplierId + "," + createdAt + "," + updatedAt);
            System.out.println("Item added successfully: " + itemId + "|" + itemName + "|" + quantity + "|" + supplierId + "|" + createdAt + "|" + updatedAt);
            writer.close();
        }
        catch (Exception e) {
            System.out.println("Error adding item to text file: " + e.getMessage());
        }
    }

    public void updateItem() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(itemDetailsFile));
            ArrayList<String> items = new ArrayList<>();
            items.add(reader.readLine());
            System.out.println("Item details read successfully:"+items);
//            for (item: items) {
//                if (item[0].equals(itemId)) {
//                    item[1] = itemName;
//                    item[2] = String.valueOf(quantity);
//                    item[3] = supplierId;
//                    item[4] = createdAt;
//                    item[5] = updatedAt;
//                }
//                items.add(String.join(",", item));
//            }
            reader.close();
//            BufferedWriter writer = new BufferedWriter(new FileWriter(itemDetailsFile));
//            for (String item : items) {
//                writer.write(item);
//                writer.newLine();
//            }
//            writer.close();
//            System.out.println("Item updated successfully: " + itemId + "|" + itemName + "|" + quantity + "|" + supplierId);
        }
        catch (Exception e) {
            System.out.println("Error updating item in text file: " + e.getMessage());
        }
    }

    public void deleteItem() {
        // Logic to delete an item
    }

    public void viewItems() {
        // Logic to view all items
    }
}
