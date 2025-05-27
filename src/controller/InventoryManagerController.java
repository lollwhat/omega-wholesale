package controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class InventoryManagerController {
    private ItemController itemController;
    private StockController stockController;

    private static final String PURCHASE_ORDER_FILE = "data/po_details.txt";

    public InventoryManagerController(ItemController itemController, StockController stockController) {
        this.itemController = itemController;
        this.stockController = stockController;
    }

    public StockController getStockController() {
        return stockController;
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
//                        columns[4], // price
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

    // load stocks - Inventory Management
    public Object[][] loadStocks() {
        Path stockFilePath = Paths.get("data/stock_details.txt");
        List<String> stockLines;

        try {
            stockLines = Files.readAllLines(stockFilePath);
            if (stockLines.isEmpty()) {
                return null;
            }

            List<String> updatedStockLines = new ArrayList<>();
            Object[][] stockData = stockLines.stream()
                    .map(line -> {
                        String[] columns = line.split(",");
                        String stockId = columns[0].trim();
                        String name = columns[1].trim();
                        int currentStock = Integer.parseInt(columns[2].trim());
                        int minStock = Integer.parseInt(columns[3].trim());
                        int maxStock = Integer.parseInt(columns[4].trim());
                        String status;
                        String lastUpdated = columns[6].trim();

                        // calculate the status
                        if (currentStock == 0) {
                            status = "Out of Stock";
                        } else if (currentStock < 40) {
                            status = "Low Stock";
                        } else {
                            status = "In Stock";
                        }

                        // update the line with the new status
                        updatedStockLines.add(String.join(",", stockId, name, String.valueOf(currentStock),
                                String.valueOf(minStock), String.valueOf(maxStock), status, lastUpdated));

                        return new Object[]{
                                stockId, name, currentStock, minStock, maxStock, status, lastUpdated
                        };
                    })
                    .toArray(Object[][]::new);

            Files.write(stockFilePath, updatedStockLines);

            return stockData;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // load approved purchase orders - inventory management
    public Object[][] loadApprovedPurchaseOrders() {
        List<Object[]> approvedPurchaseOrders = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(PURCHASE_ORDER_FILE));
            for (String line : lines) {
                String[] columns = line.split(",");
                if ("Approved".equals(columns[6].trim())) {
                    approvedPurchaseOrders.add(new Object[]{
                            columns[0].trim(), // PO ID
                            columns[1].trim(), // date
                            columns[2].trim(), // requisition
                            columns[3].trim(), // item
                            columns[4].trim(), // quantity
                            columns[5].trim(), // supplier
                            columns[6].trim(), // status
                            columns[7].trim(), // created by
                            columns[8].trim()  // updated by
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading approved purchase orders: " + e.getMessage());
        }

        Object[][] result = new Object[approvedPurchaseOrders.size()][];
        for (int i = 0; i < approvedPurchaseOrders.size(); i++) {
            result[i] = approvedPurchaseOrders.get(i);
        }

        return result;
    }

    // column names for table header - View Items
    public String[] getItemTableColumns() {
        return new String[]{"Item ID", "Item Name", "Status", "Stock"};
    }

    // column names for table header - View Purchase Orders
    public String[] getPurchaseOrderTableColumns() {
        return new String[]{"PO ID", "Date", "PO Requisition", "Item", "Quantity", "Supplier", "Created by", "Updated by"};
    }

    // columns names for table header - Inventory Management (Approved Purchase Orders)
    public  String[] getApprovedPurchaseOrderTableColumns() {
        return new String[]{"PO ID", "Date", "PO Requisition", "Item", "Quantity", "Supplier", "Status", "Created by", "Approved by", "Action"};
    }

    // columns names for table header - Inventory Management (Stock)
    public String[] getStockTableColumns() {
        return new String[]{"Item ID", "Name", "Current Stock", "Min Stock", "Max Stock", "Status", "Last Updated"};
    }
}
