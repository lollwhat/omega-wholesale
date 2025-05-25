package controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PurchaseManagerController {

    private static final String PO_FILE = "data/po_details.txt";
    private static final String ITEM_FILE = "data/item_details.txt";
    private static final String SUPPLIER_FILE = "data/supplier_details.txt";
    private static final String REQ_FILE = "data/purchase_requisition.txt";

    private ItemController itemController;
    private SupplierController supplierController;
    private PurchaseRequisitionController requisitionController;  // <-- here

    public PurchaseManagerController(ItemController itemController, SupplierController supplierController, PurchaseRequisitionController requisitionController) {
        this.itemController = itemController;
        this.supplierController = supplierController;
        this.requisitionController = requisitionController;  // <-- here
    }

    // === 1. Load Items Table ===
    public Object[][] loadItems() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(ITEM_FILE));
            if (lines.isEmpty()) return null;

            return lines.stream()
                    .map(line -> line.split(","))
                    .map(columns -> new Object[]{
                            columns[0], // Item ID
                            columns[1], // Item Name
                            columns[5], // Supplier
                            columns[4], // Price
                            columns[2]  // Stock
                    })
                    .toArray(Object[][]::new);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getItemTableColumns() {
        return new String[]{"Item ID", "Name", "Supplier", "Price", "Stock"};
    }

    // === 2. Load Suppliers Table ===
    public Object[][] loadSuppliers() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(SUPPLIER_FILE));
            if (lines.isEmpty()) return null;

            return lines.stream()
                    .map(line -> line.split(","))
                    .map(columns -> {
                        String itemsSupplied = String.join(", ", Arrays.copyOfRange(columns, 4, columns.length));
                        return new Object[]{
                                columns[0], // Supplier ID
                                columns[1], // Name
                                columns[2], // Contact
                                columns[4], // Email
                                columns.length > 8 ? columns[8] : ""  // Items Supplied, check length to avoid IndexOutOfBounds
                        };
                    })
                    .toArray(Object[][]::new);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getSupplierTableColumns() {
        return new String[]{"Supplier ID", "Name", "Contact", "Email", "Items Supplied"};
    }

    public Object[][] loadRequisitions() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REQ_FILE));
            if (lines.isEmpty()) return null;

            return lines.stream()
                    .map(line -> line.split(",", -1))
                    .filter(columns -> columns.length >= 15)
                    .map(columns -> new Object[]{
                            columns[0],                               // PR ID
                            columns[12].split(" ")[0],               // Date (only YYYY-MM-DD)
                            columns[14].split(" ")[0],               // Required Date (only YYYY-MM-DD)
                            columns[2],                               // Item Name
                            columns[4],                               // Quantity
                            columns[8],                               // Supplier Name
                            columns[11],                              // Status
                            columns[13]                               // Created By
                    })
                    .toArray(Object[][]::new);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getRequisitionTableColumns() {
        return new String[]{
                "PR ID", "Date", "Required Date", "Item",
                "Quantity", "Supplier", "Status", "Created By"
        };
    }

    // === 4. Load Purchase Orders Table ===
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
                            rowWithActions[row.length] = "Actions"; // Placeholder for UI actions
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
}
