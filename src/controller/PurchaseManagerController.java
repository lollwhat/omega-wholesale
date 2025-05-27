package controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PurchaseManagerController {

    private static final String PO_FILE = "data/po_details.txt";
    private static final String ITEM_FILE = "data/item_details.txt";
    private static final String SUPPLIER_FILE = "data/supplier_details.txt";
    private static final String REQ_FILE = "data/requisition_details.txt";

    private ItemController itemController;
    private SupplierController supplierController;

    public PurchaseManagerController(ItemController itemController, SupplierController supplierController) {
        this.itemController = itemController;
        this.supplierController = supplierController;
    }

    // === 1. Load Items Table ===
    public Object[][] loadItems() {
        if (itemController == null) return null;

        List<String> items = itemController.getAll();
        if (items == null || items.isEmpty()) return null;

        return items.stream()
                .map(line -> line.split(","))
                .map(columns -> new Object[]{
                        columns[0], // Item ID
                        columns[1], // Item Name
                        columns[5], // Supplier
                        columns[4], // Price
                        columns[2]  // Stock
                })
                .toArray(Object[][]::new);
    }

    public String[] getItemTableColumns() {
        return new String[]{"Item ID", "Name", "Supplier", "Price", "Stock"};
    }

    // === 2. Load Suppliers Table ===
    public Object[][] loadSuppliers() {
        if (supplierController == null) return null;

        List<String> suppliers = supplierController.getAll();
        if (suppliers == null || suppliers.isEmpty()) return null;

        return suppliers.stream()
                .map(line -> line.split(","))
                .map(columns -> {
                    String itemsSupplied = String.join(", ", Arrays.copyOfRange(columns, 4, columns.length));
                    return new Object[]{
                            columns[0], // Supplier ID
                            columns[1], // Name
                            columns[2], // Contact
                            columns[4], // Email
                            columns[8]  // Items Supplied
                    };
                })
                .toArray(Object[][]::new);
    }

    public String[] getSupplierTableColumns() {
        return new String[]{"Supplier ID", "Name", "Contact", "Email", "Items Supplied"};
    }

    // === 3. Load Purchase Orders Table ===
    public Object[][] loadPurchaseOrders(boolean isForGeneratePage) {
        try {
            List<String> purchaseOrders = Files.readAllLines(Paths.get(PO_FILE));
            if (purchaseOrders == null || purchaseOrders.isEmpty()) return null;

            return purchaseOrders.stream()
                    .map(line -> line.split(","))
                    .filter(columns -> columns.length >= 9) // prevent index errors
                    .map(columns -> {
                        Object[] row = {
                                columns[0], // PO ID
                                columns[1], // PO Date
                                columns[2], // Requisition
                                columns[3], // Item
                                columns[4], // Quantity
                                columns[5], // Supplier
                                columns[6], // Status
                                columns[7], // Created By
                                columns[8]  // Approved By
                        };
                        if (isForGeneratePage) {
                            Object[] rowWithActions = Arrays.copyOf(row, row.length + 1);
                            rowWithActions[row.length] = "Actions"; // Placeholder
                            return rowWithActions;
                        } else {
                            return row;
                        }
                    })
                    .toArray(Object[][]::new);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getPurchaseOrderTableColumns(boolean isForGeneratePage) {
        if (isForGeneratePage) {
            return new String[]{"PO ID", "PO Date", "Requisition", "Item", "Quantity",
                    "Supplier", "Status", "Created By", "Approved By", "Actions"};
        } else {
            return new String[]{"PO ID", "PO Date", "Requisition", "Item", "Quantity",
                    "Supplier", "Status", "Created By", "Approved By"};
        }
    }

    // === 4. Generate New PO ID ===
    public static String generateNewPOID() {
        int maxID = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("PO")) {
                    String id = line.split(",")[0].substring(2).trim();
                    maxID = Math.max(maxID, Integer.parseInt(id));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.format("PO%03d", maxID + 1);
    }

    // === 5. Load Item Dropdown Options ===
    public static List<String> loadItemOptions() {
        List<String> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ITEM_FILE))) {
            String line;
            Set<String> seen = new HashSet<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && !seen.contains(parts[0])) {
                    items.add(parts[1].trim() + " (" + parts[0].trim() + ")");
                    seen.add(parts[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    // === 6. Load Supplier Dropdown Options ===
    public static List<String> loadSupplierOptions() {
        List<String> suppliers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SUPPLIER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    suppliers.add(parts[1].trim() + " (" + parts[0].trim() + ")");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public static List<String> loadRequisitionOptions() {
        List<String> requisitions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(REQ_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String reqID    = parts[0].trim();
                    String item     = parts[3].trim();
                    String quantity = parts[4].trim();
                    requisitions.add(reqID + " - " + item + " (" + quantity + ")");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // default at top
        requisitions.add(0, "Direct Order (No Requisition)");
        return requisitions;
    }
    public static boolean saveNewPO(String poID, String date, String requisition, String itemDisplay, String qty, String supplierDisplay) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PO_FILE, true))) {
            String itemName = itemDisplay.split("\\(")[0].trim();
            String supplierName = supplierDisplay.split("\\(")[0].trim();

            // Default status and departments
            String status = "Pending";
            String requestDept = "purchase";
            String approvalDept = "";

            String line = String.join(", ",
                    poID, date, requisition, itemName, qty, supplierName, status, requestDept, approvalDept
            );

            bw.write(line);
            bw.newLine();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}