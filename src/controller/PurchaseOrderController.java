package controller;

import model.PurchaseOrder;
import model.PurchaseOrderItem;
import model.EntityType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrderController extends CRUDController<PurchaseOrder> {
    private FileController poItemsFileController;
    private static final String PO_ITEMS_FILE_PATH = "data/purchase_order_item.txt";
    private static final String PO_HEADER_FILE_PATH = "data/purchase_order.txt";

    public PurchaseOrderController() {
        super(PO_HEADER_FILE_PATH, EntityType.PURCHASE_ORDER);
        this.poItemsFileController = new FileController(PO_ITEMS_FILE_PATH);
        try {
            File itemsFile = new File(PO_ITEMS_FILE_PATH);
            if (!itemsFile.exists()) {
                if (itemsFile.getParentFile() != null && !itemsFile.getParentFile().exists()) {
                    itemsFile.getParentFile().mkdirs();
                }
                itemsFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("CRITICAL: Error ensuring PO items file exists at " + PO_ITEMS_FILE_PATH + ": " + e.getMessage());
            throw new RuntimeException("Failed to initialize PO items data file.", e);
        }
    }

    public String getPoHeaderFilePath() {
        return PO_HEADER_FILE_PATH;
    }

    public String getPoItemsFilePath() {
        return PO_ITEMS_FILE_PATH;
    }

    private void appendLineToFile(String filePath, String data) throws IOException {
        boolean needsNewLine = false;
        File file = new File(filePath);
        if (file.exists() && file.length() > 0) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                if (raf.length() > 0) {
                    raf.seek(raf.length() - 1);
                    char lastChar = (char) raf.read();
                    if (lastChar != '\n' && lastChar != '\r') {
                        needsNewLine = true;
                    }
                }
            }
        } else if (!file.exists()){
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            needsNewLine = false;
        }

        try (FileWriter fw = new FileWriter(filePath, true)) {
            if (needsNewLine) {
                fw.write(System.lineSeparator());
            }
            fw.write(data);
            fw.write(System.lineSeparator());
        }
    }

    private void overwriteFileWithLines(String filePath, List<String> lines) throws IOException {
        try (FileWriter fw = new FileWriter(filePath, false)) {
            for (int i = 0; i < lines.size(); i++) {
                fw.write(lines.get(i));
                if (i < lines.size()) {
                    fw.write(System.lineSeparator());
                }
            }
        }
    }

    @Override
    public void add(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null || purchaseOrder.getPoId() == null) {
            throw new IllegalArgumentException("Purchase Order or its ID cannot be null for add operation.");
        }
        try {
            String headerData = purchaseOrder.toCSV();
            appendLineToFile(PO_HEADER_FILE_PATH, headerData);
            System.out.println("PO Header added: " + purchaseOrder.getPoId());

            if (purchaseOrder.getItems() != null) {
                for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                    item.setPoId(purchaseOrder.getPoId());
                    appendLineToFile(PO_ITEMS_FILE_PATH, item.toCSV());
                }
                System.out.println(purchaseOrder.getItems().size() + " item(s) added for PO: " + purchaseOrder.getPoId());
            }
        } catch (IOException e) {
            System.err.println("Error adding full purchase order (ID: " + purchaseOrder.getPoId() + ") to file: " + e.getMessage());
            throw new RuntimeException("Failed to save purchase order: " + purchaseOrder.getPoId(), e);
        }
    }

    public PurchaseOrder createPurchaseOrderFromPR(String prId, String notes, List<PurchaseOrderItem> itemsData) {
        String poId;
        try {
            new FileController(PO_HEADER_FILE_PATH);
            List<String> allPoHeaders = FileController.getFile();
            int nextIdNumber = 1;
            if (allPoHeaders != null && !allPoHeaders.isEmpty()) {
                int maxId = 0;
                for (String line : allPoHeaders) {
                    String[] parts = line.split(",");
                    if (parts.length > 0 && parts[0].startsWith("PO")) {
                        try {
                            maxId = Math.max(maxId, Integer.parseInt(parts[0].substring(2)));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing PO ID from line: " + line + " - " + e.getMessage());
                        }
                    }
                }
                nextIdNumber = maxId + 1;
            }
            poId = "PO" + String.format("%03d", nextIdNumber);
        } catch (Exception e) {
            System.err.println("Error generating PO ID: " + e.getMessage());
            throw new RuntimeException("Could not generate PO ID", e);
        }

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();
        int initialStatus = 0;

        PurchaseOrder newPO = new PurchaseOrder(poId, prId, notes, initialStatus, createdAt, createdBy, createdAt, createdBy, null, null);

        if (itemsData != null) {
            for (PurchaseOrderItem item : itemsData) {
                newPO.addItem(new PurchaseOrderItem(
                        poId,
                        item.getItemId(),
                        item.getItemCode(),
                        item.getItemName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSelectedSupplierId()
                ));
            }
        }
        add(newPO);
        System.out.println("New Purchase Order " + poId + " created successfully from PR " + prId + " with items from various suppliers.");
        return newPO;
    }

    @Override
    public void update(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null || purchaseOrder.getPoId() == null) {
            throw new IllegalArgumentException("Purchase Order or its ID cannot be null for update operation.");
        }
        try {
            // 1. Update PO Header
            new FileController(PO_HEADER_FILE_PATH);
            super.fileController.updateFile(purchaseOrder.toCSV());
            System.out.println("PO Header updated: " + purchaseOrder.getPoId());

            // 2. Update PO Items
            new FileController(PO_ITEMS_FILE_PATH);
            List<String> allItemLines = FileController.getFile();
            List<String> itemsToKeep = new ArrayList<>();
            if (allItemLines != null) {
                for (String itemLine : allItemLines) {
                    String[] parts = itemLine.split(",");
                    if (parts.length > 0 && !parts[0].equals(purchaseOrder.getPoId())) {
                        itemsToKeep.add(itemLine);
                    }
                }
            }
            overwriteFileWithLines(PO_ITEMS_FILE_PATH, itemsToKeep);
            System.out.println("Old items processed for PO: " + purchaseOrder.getPoId());

            if (purchaseOrder.getItems() != null) {
                for (PurchaseOrderItem item : purchaseOrder.getItems()) {
                    item.setPoId(purchaseOrder.getPoId());
                    appendLineToFile(PO_ITEMS_FILE_PATH, item.toCSV());
                }
                System.out.println(purchaseOrder.getItems().size() + " new/updated item(s) added for PO: " + purchaseOrder.getPoId());
            }
        } catch (IOException e) {
            System.err.println("Error updating full purchase order (ID: " + purchaseOrder.getPoId() + ") in file: " + e.getMessage());
            throw new RuntimeException("Failed to update purchase order: " + purchaseOrder.getPoId(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error during update of PO (ID: " + purchaseOrder.getPoId() + "): " + e.getMessage());
            throw new RuntimeException("Failed to update purchase order: " + purchaseOrder.getPoId(), e);
        }
    }

    public boolean updatePurchaseOrderStatus(String poId, int newStatus) {
        new FileController(PO_HEADER_FILE_PATH);
        String userId = SessionController.getInstance().getUserId();
        PurchaseOrder po = getPurchaseOrderHeaderById(poId);
        if (po == null) {
            System.err.println("Purchase Order " + poId + " not found for status update.");
            return false;
        }

        po.setStatus(newStatus);
        po.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        po.setUpdatedBy(userId);

        if (newStatus == 2) {
            po.setReceivedAt(po.getUpdatedAt());
            po.setReceivedBy(userId);
        } else if (newStatus == 0 || newStatus == 1 || newStatus == 3) {
            po.setReceivedAt(null);
            po.setReceivedBy(null);
        }

        try {
            new FileController(PO_HEADER_FILE_PATH);
            super.fileController.updateFile(po.toCSV());
            System.out.println("Purchase Order " + poId + " status updated to " + newStatus);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating PO status for " + poId + ": " + e.getMessage());
            return false;
        }
    }


    public PurchaseOrder getPurchaseOrderHeaderById(String poId) {
        try {
            new FileController(PO_HEADER_FILE_PATH);
            String[] headerParts = super.fileController.getLine(0, poId);
            if (headerParts != null) {
                return PurchaseOrder.fromCSV(String.join(",", headerParts));
            }
        } catch (IOException e) {
            System.err.println("Error reading PO header for ID " + poId + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error reading PO header for ID " + poId + ": " + e.getMessage());
        }
        return null;
    }

    public PurchaseOrder getFullPurchaseOrderById(String poId) {
        PurchaseOrder poHeader = getPurchaseOrderHeaderById(poId);
        if (poHeader == null) {
            return null;
        }
        List<PurchaseOrderItem> poItems = new ArrayList<>();
        try {
            new FileController(PO_ITEMS_FILE_PATH);
            List<String> itemLines = FileController.getFile();
            if (itemLines != null) {
                for (String line : itemLines) {
                    String[] parts = line.split(",");
                    if (parts.length > 0 && parts[0].equals(poId)) {
                        PurchaseOrderItem item = PurchaseOrderItem.fromCSV(line);
                        if (item != null) {
                            poItems.add(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading PO items for PO ID " + poId + ": " + e.getMessage());
        }
        poHeader.setItems(poItems);
        return poHeader;
    }

    public List<PurchaseOrder> getAllPurchaseOrderHeaders() {
        List<PurchaseOrder> poHeaders = new ArrayList<>();
        try {
            new FileController(PO_HEADER_FILE_PATH);
            List<String> lines = FileController.getFile();
            if (lines != null) {
                for (String line : lines) {
                    PurchaseOrder po = PurchaseOrder.fromCSV(line);
                    if (po != null) {
                        poHeaders.add(po);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading all PO headers: " + e.getMessage());
        }
        return poHeaders;
    }

    public List<PurchaseOrder> getAllFullPurchaseOrders() {
        List<PurchaseOrder> poHeaders = getAllPurchaseOrderHeaders();
        List<PurchaseOrder> fullPOs = new ArrayList<>(poHeaders.size());

        Map<String, List<PurchaseOrderItem>> allItemsMap = new HashMap<>();
        try {
            new FileController(PO_ITEMS_FILE_PATH);
            List<String> itemLines = FileController.getFile();
            if (itemLines != null) {
                for (String line : itemLines) {
                    PurchaseOrderItem item = PurchaseOrderItem.fromCSV(line);
                    if (item != null && item.getPoId() != null) {
                        allItemsMap.computeIfAbsent(item.getPoId(), k -> new ArrayList<>()).add(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading all PO items: " + e.getMessage());
        }

        for (PurchaseOrder header : poHeaders) {
            PurchaseOrder fullPO = PurchaseOrder.fromCSV(header.toCSV());
            if (fullPO != null) {
                fullPO.setItems(allItemsMap.getOrDefault(header.getPoId(), new ArrayList<>()));
                fullPOs.add(fullPO);
            }
        }
        return fullPOs;
    }

    @Override
    public void delete(String poId) {
        if (poId == null || poId.trim().isEmpty()) {
            throw new IllegalArgumentException("PO ID cannot be null or empty for delete operation.");
        }
        try {
            // 1. Delete PO Header
            new FileController(PO_HEADER_FILE_PATH);
            super.fileController.deleteLine(poId, 0);

            // 2. Delete PO Items
            new FileController(PO_ITEMS_FILE_PATH);
            List<String> allItemLines = FileController.getFile();
            List<String> itemsToKeep = new ArrayList<>();
            boolean itemsModified = false;
            if (allItemLines != null) {
                for (String itemLine : allItemLines) {
                    String[] parts = itemLine.split(",");
                    if (parts.length > 0 && parts[0].equals(poId)) {
                        itemsModified = true;
                    } else {
                        itemsToKeep.add(itemLine);
                    }
                }
            }

            if (itemsModified) {
                overwriteFileWithLines(PO_ITEMS_FILE_PATH, itemsToKeep);
                System.out.println("Items associated with PO " + poId + " deleted.");
            } else {
                System.out.println("No items found for PO " + poId + " to delete from items file.");
            }

        } catch (IOException e) {
            System.err.println("Error deleting purchase order (ID: " + poId + "): " + e.getMessage());
            throw new RuntimeException("Failed to delete purchase order: " + poId, e);
        } catch (Exception e) {
            System.err.println("Unexpected error during deletion of PO (ID: " + poId + "): " + e.getMessage());
            throw new RuntimeException("Failed to delete purchase order: " + poId, e);
        }
    }
}