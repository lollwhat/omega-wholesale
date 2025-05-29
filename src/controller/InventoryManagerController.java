package controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManagerController {
    private ItemController itemController;
    private StockController stockController;

    private static final String PURCHASE_ORDER_FILE = "data/purchase_order.txt";

    public InventoryManagerController(ItemController itemController, StockController stockController) {
        this.itemController = itemController;
        this.stockController = stockController;
    }

    public StockController getStockController() {
        return stockController;
    }

    // laod items - View Items Button
    public Object[][] loadItems() {
        List<Object[]> items = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("data/purchase_order_item.txt"));
            for (String line : lines) {
                String[] columns = line.split(",");
                if (columns.length >= 7) {
                    items.add(new Object[]{
                            columns[1], // item ID
                            columns[3], // item name
                            columns[5], // unit price
                            columns[4], // quantity
                            columns[6]  // total price
                    });
                } else {
                    System.err.println("Malformed line in purchase_order_item.txt: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading items: " + e.getMessage());
        }

        if (items.isEmpty()) {
            return null;
        }

        return items.toArray(new Object[0][]);
    }

    // load purchase orders - View Purchase Orders button
    public Object[][] loadPurchaseOrders() {
        List<Object[]> purchaseOrders = new ArrayList<>();
        Map<String, String> supplierMap = new HashMap<>();
        Map<String, List<String[]>> poItemsMap = new HashMap<>();

        try {
            List<String> supplierLines = Files.readAllLines(Paths.get("data/supplier_details.txt"));
            for (String supplierLine : supplierLines) {
                String[] supplierDetails = supplierLine.split(",");
                if (supplierDetails.length >= 2) {
                    String supplierId = supplierDetails[0].trim();
                    String supplierName = supplierDetails[1].trim();
                    supplierMap.put(supplierId, supplierName);
                }
            }

            // load purchase order items and group by PO ID
            List<String> itemLines = Files.readAllLines(Paths.get("data/purchase_order_item.txt"));
            for (String itemLine : itemLines) {
                String[] itemDetails = itemLine.split(",");
                if (itemDetails.length >= 5) {
                    String poId = itemDetails[0].trim();
                    poItemsMap.computeIfAbsent(poId, k -> new ArrayList<>()).add(itemDetails);
                }
            }

            // load purchase orders and join with supplier details and items
            List<String> poLines = Files.readAllLines(Paths.get("data/purchase_order.txt"));
            for (String poLine : poLines) {
                String[] poDetails = poLine.split(",");
                if (poDetails.length >= 11) {
                    String poId = poDetails[0].trim();
                    String supplierName = supplierMap.getOrDefault(poDetails[3].trim(), "Unknown Supplier");
                    List<String[]> items = poItemsMap.getOrDefault(poId, new ArrayList<>());

                    if (items.isEmpty()) {
                        purchaseOrders.add(new Object[]{
                                poDetails[0], // PO ID
                                poDetails[1], // PR ID
                                supplierName, // supplier
                                "N/A",        // item name
                                poDetails[6], // created at
                                poDetails[7], // created by
                                poDetails[8], // updated at
                                poDetails[9], // updated by
                                poDetails[2]  // comments
                        });
                    } else {
                        for (String[] item : items) {
                            purchaseOrders.add(new Object[]{
                                    poDetails[0], // PO ID
                                    poDetails[1], // PR ID
                                    supplierName, // supplier
                                    item[3],      // item name
                                    poDetails[6], // created at
                                    poDetails[7], // created by
                                    poDetails[8], // updated at
                                    poDetails[9], // updated by
                                    poDetails[2]  // comments
                            });
                        }
                    }
                } else {
                    System.err.println("Malformed line in purchase_order.txt: " + poLine);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading purchase orders: " + e.getMessage());
        }

        return purchaseOrders.isEmpty() ? null : purchaseOrders.toArray(new Object[0][]);
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
//    public Object[][] loadApprovedPurchaseOrders() {
//        List<Object[]> approvedPurchaseOrders = new ArrayList<>();
//        try {
//            List<String> lines = Files.readAllLines(Paths.get(PURCHASE_ORDER_FILE));
//            for (String line : lines) {
//                String[] columns = line.split(",");
//                String statusString = getStatusString(columns[4].trim());
//                if ("Processing".equalsIgnoreCase(statusString)) {
//                    approvedPurchaseOrders.add(new Object[]{
//                            columns[0].trim(), // PO ID
//                            columns[1].trim(), // PR ID
//                            columns[2].trim(), // comments
//                            columns[3].trim(), // supplier ID
//                            statusString,      // status as a string
//                            columns[5].trim(), // created at
//                            columns[6].trim(), // created by
//                            columns[7].trim(), // updated at
//                            columns[8].trim(), // updated by
//                            columns[9] == null || columns[9].trim().isEmpty() ? "N/A" : columns[9].trim(), // received at
//                            columns[10] == null || columns[10].trim().isEmpty() ? "N/A" : columns[10].trim()  // received by
//                    });
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Error loading approved purchase orders: " + e.getMessage());
//        }
//
//        // convert the list of approved purchase orders to a 2D array
//        return approvedPurchaseOrders.toArray(new Object[0][]);
//    }

    // load approved purchase orders with items - inventory management
    public Object[][] loadApprovedPurchaseOrdersWithItems() {
        List<Object[]> combinedData = new ArrayList<>();
        Map<String, List<String[]>> poItemsMap = new HashMap<>();

        try {
            // read purchase_order_item.txt and group items by PO ID
            List<String> itemLines = Files.readAllLines(Paths.get("data/purchase_order_item.txt"));
            for (String itemLine : itemLines) {
                String[] itemDetails = itemLine.split(",");
                for (int i = 0; i < itemDetails.length; i++) {
                    itemDetails[i] = itemDetails[i].trim();
                }
                String poId = itemDetails[0];
                poItemsMap.computeIfAbsent(poId, k -> new ArrayList<>()).add(itemDetails);
            }

            // read purchase_order.txt and join with items for "Processing" POs only
            List<String> poLines = Files.readAllLines(Paths.get("data/purchase_order.txt"));
            for (String poLine : poLines) {
                String[] poDetails = poLine.split(",");
                for (int i = 0; i < poDetails.length; i++) {
                    poDetails[i] = poDetails[i].trim();
                }
                String poId = poDetails[0];
                String status = poDetails[4]; // status field

                // include only "Approved" POs (status code "1")
                if (!"1".equals(status)) {
                    continue;
                }

                // get items for this PO ID
                List<String[]> items = poItemsMap.getOrDefault(poId, new ArrayList<>());

                // if no items exist for this PO, still add the metadata row with empty item details
                if (items.isEmpty()) {
                    combinedData.add(new Object[]{
                            poDetails[0], // PO ID
                            poDetails[1], // PR ID
                            poDetails[2], // comments
                            poDetails[3], // supplier ID
                            poDetails[4], // status
                            poDetails[5], // created at
                            poDetails[6], // created by
                            poDetails[7], // updated at
                            poDetails[8], // updated by
                            "N/A",        // item ID
                            "N/A",        // item name
                            "N/A"         // quantity
                    });
                } else {
                    for (String[] item : items) {
                        combinedData.add(new Object[]{
                                poDetails[0], // PO ID
                                poDetails[1], // PR ID
                                poDetails[2], // comments
                                poDetails[3], // supplier ID
                                poDetails[4], // status
                                poDetails[5], // created at
                                poDetails[6], // created by
                                poDetails[7], // updated at
                                poDetails[8], // updated by
                                item[1],      // item ID
                                item[3],      // item name
                                item[4]       // quantity
                        });
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading purchase orders with items: " + e.getMessage());
        }

        // convert the combined list to a 2D array
        return combinedData.toArray(new Object[0][]);
    }

    private String getStatusString(String statusValue) {
        try {
            int statusInt = Integer.parseInt(statusValue.trim());
            return switch (statusInt) {
                case 0 -> "Processing";
                case 1 -> "Approved";
                case 2 -> "Received";
                case 3 -> "Cancelled";
                default -> "Unknown (" + statusInt + ")";
            };
        } catch (NumberFormatException e) {
            return "Invalid Status";
        }
    }

    // column names for table header - View Items
    public String[] getItemTableColumns() {
        return new String[]{"Item ID", "Item Name", "Unit Price", "Quantity", "Total Price"};
    }

    // column names for table header - View Purchase Orders
    public String[] getPurchaseOrderTableColumns() {
        return new String[]{"PO ID", "PR ID", "Supplier", "Item Name", "Created At", "Created By", "Updated At",
                            "Updated By", "Comments"
        };
    }

    // columns names for table header - Inventory Management (Approved Purchase Orders)
//    public String[] getApprovedPurchaseOrderTableColumns() {
//        return new String[]{"PO ID", "PR ID", "Comments", "Supplier ID", "Status", "Created At", "Created By", "Updated At",
//                            "Updated By", "Received At", "Received By", "Action",
//        };
//    }

    // columns names for table header - Inventory Management (Stock)
    public String[] getStockTableColumns() {
        return new String[]{"Item ID", "Name", "Current Stock", "Min Stock", "Max Stock", "Status", "Last Updated", "Actions"};
    }

    // columns names for table header - Inventory Management (POs with Items)
    public String[] getPurchaseOrderWithItemsTableColumns() {
        return new String[]{"PO ID", "PR ID", "Comments", "Supplier ID", "Status", "Created At", "Created By", "Updated At",
                            "Updated By", "Item ID", "Item Name", "Quantity", "Action"
        };
    }
}
