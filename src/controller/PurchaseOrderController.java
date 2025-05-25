package controller;

import model.EntityType;
import model.PurchaseOrder;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderController extends CRUDController<PurchaseOrder> {

    private static final String FILE_PATH = "data/po_details.txt";

    public PurchaseOrderController() {
        super(FILE_PATH, EntityType.PURCHASE_ORDER);
    }

    @Override
    public void add(PurchaseOrder purchaseOrder) {
        try {
            String data = purchaseOrder.toCSV();
            appendToFile(FILE_PATH, data);
            System.out.println("Purchase order added successfully");
        } catch (Exception e) {
            System.out.println("Error adding purchase order to file: " + e.getMessage());
        }
    }

    public boolean createPurchaseOrder(
            String poID, String poDate, String requisition, String itemName,
            int quantity, String supplierName, String status, String createdBy, String approvedBy) {

        if (poID == null || poID.isEmpty()) {
            poID = generateNewPOID();
        } else if (doesPOIDExist(poID)) {
            JOptionPane.showMessageDialog(null,
                    "A Purchase Order with ID " + poID + " already exists.",
                    "Duplicate PO ID",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder(
                poID, poDate, requisition, itemName, quantity, supplierName,
                status, createdBy, approvedBy
        );

        add(purchaseOrder);
        return true;
    }

    @Override
    public void update(PurchaseOrder purchaseOrder) {
        try {
            List<String> lines = readAllLines(FILE_PATH);
            String updatedLine = purchaseOrder.toCSV();
            String poID = purchaseOrder.getPoId();

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(poID + ",")) {
                    lines.set(i, updatedLine);
                    break;
                }
            }

            writeAllLines(FILE_PATH, lines);
            System.out.println("Purchase order updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating purchase order in file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String poID) {
        try {
            List<String> lines = readAllLines(FILE_PATH);
            boolean removed = lines.removeIf(line -> line.startsWith(poID + ","));
            if (removed) {
                writeAllLines(FILE_PATH, lines);
                System.out.println("Purchase order deleted successfully.");
            } else {
                System.out.println("Purchase order not found: " + poID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean doesPOIDExist(String poID) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(poID + ",")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void appendToFile(String filename, String data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(data);
            writer.newLine();
        }
    }

    private List<String> readAllLines(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void writeAllLines(String filename, List<String> lines) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }

    public static List<String> loadSupplierOptions() {
        List<String> options = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("data/supplier_details.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    options.add(parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading suppliers: " + e.getMessage());
        }
        return options;
    }

    public static List<String> loadItemOptions() {
        List<String> options = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("data/item_details.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    options.add(parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading items: " + e.getMessage());
        }
        return options;
    }

    public static List<String> loadRequisitionOptions() {
        List<String> options = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("data/purchase_requisition.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1) {
                    options.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading requisitions: " + e.getMessage());
        }
        return options;
    }

    public static String getSupplierForRequisition(String requisitionID) {
        if (requisitionID == null || requisitionID.isEmpty()) return "";

        try (BufferedReader br = new BufferedReader(new FileReader("data/purchase_requisition.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 8 && parts[0].equals(requisitionID)) {
                    return parts[7];  // supplier ID here, not parts[5]
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static class RequisitionDetails {
        public String requisitionID;
        public String requestDate;
        public String requiredDate;
        public String item;
        public int quantity;
        public String supplier;
        public String status;
        public String requestedBy;

        public RequisitionDetails(String[] fields) {
            this.requisitionID = fields[0];         // PR001
            this.requestDate = fields[1];           // 2025-05-22
            this.requiredDate = fields[2];          // 2025-06-01
            this.item = fields[3];                  // Seeder Item ID A
            this.quantity = Integer.parseInt(fields[4]); // 20
            this.supplier = fields[7];              // SUP001
            this.status = fields[11];               // approved
            this.requestedBy = fields[13];          // sales1
        }
    }

    public static RequisitionDetails getRequisitionDetails(String requisitionID) {
        if (requisitionID == null) return null;

        final String filePath = "data/purchase_requisition.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 14 && parts[0].trim().equals(requisitionID.trim())) {
                    return new RequisitionDetails(parts);
                }
            }
        } catch (IOException e) {
            // You can replace this with a proper logger if desired
            e.printStackTrace();
        }
        return null;
    }

    public static String generateNewPOID() {
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("PO")) {
                    String[] parts = line.split(",", -1);
                    try {
                        int num = Integer.parseInt(parts[0].substring(2));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.format("PO%03d", max + 1);
    }

    public String[] getPurchaseOrderDataById(String poID) {
        try (BufferedReader br = new BufferedReader(new FileReader("data/po_details.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(poID + ",")) {
                    return line.split(",", -1); // includes empty fields
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePurchaseOrder(String poID, String date, String requisition, String item,
                                       int quantity, String supplier, String status, String createdBy, String approvedBy) {
        try {
            List<String> lines = readAllLines(FILE_PATH);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length < 9) continue;

                if (parts[0].trim().equals(poID)) {
                    // Build updated CSV line with all fields (createdBy and approvedBy included)
                    String updatedLine = String.join(",",
                            poID,
                            date,
                            requisition,
                            item,
                            String.valueOf(quantity),
                            supplier,
                            status,
                            createdBy,
                            approvedBy
                    );
                    lines.set(i, updatedLine);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                writeAllLines(FILE_PATH, lines);
            }

            return updated;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

