package controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class InventoryManagerController {
    private ItemController itemController;
    private static final String PURCHASE_ORDER_FILE = "data/po_details.txt";

    public InventoryManagerController(ItemController itemController) {
        this.itemController = itemController;
    }

    // laod items - View Items Button
    public Object[][] loadItems() {
        List<String> items = itemController.getAll();
        if (items == null || items.isEmpty()) {
            return null;
        }

        // convert item data to 2D Object array for the table
       return items.stream()
                .map(line -> line.split(","))
                .map(columns -> new Object[] {
                        columns[0], // item ID
                        columns[1], // item name
                        columns[5], // supplier
                        columns[4], // price
                        columns[2]  // stock/quantity
                })
                .toArray(Object[][]::new);
    }

    // load purchase orders - View Purchase Orders button
    public Object[][] loadPurchaseOrders() {
        List<Object[]> purchaseOrders = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(PURCHASE_ORDER_FILE));
            for (String line : lines) {
                String[] columns = line.split(",");
                if (columns.length > 7 ) {
                    purchaseOrders.add(new Object[]{
                            columns[0], // PO ID
                            columns[1], // date
                            columns[2], // requisition
                            columns[3], // item
                            columns[4], // quantity
                            columns[5], // supplier
                            columns[6], // status
                            columns[7], // created by
                            columns[8]  // updated by
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading purchase orders: " + e.getMessage());
        }

        if (purchaseOrders.isEmpty()) {
            return null;
        }

        return purchaseOrders.toArray(new Object[0][]);
    }

    // column names for table header - View Items
    public String[] getItemTableColumns() {
        return new String[]{"Item ID", "Item Name", "Supplier", "Stock"};
    }

    // column names for table header - View Purchase Orders
    public String[] getPurchaseOrderTableColumns() {
        return new String[]{"PO ID", "Date", "PO Requisition", "Item", "Quantity", "Supplier", "Created by", "Updated by"};
    }
}
