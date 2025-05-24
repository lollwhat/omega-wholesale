package controller;

import model.PurchaseRequisition;
import model.EntityType;
import model.PurchaseRequisitionItem;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PurchaseRequisitionController extends CRUDController<PurchaseRequisition> {
    private static final String PR_ITEMS_FILE_PATH = "data/purchase_requisition_item.txt";
    private static final String PR_HEADER_FILE_PATH = "data/purchase_requisition.txt";

    public PurchaseRequisitionController() {
        super("data/purchase_requisition.txt", EntityType.PURCHASE_REQUISITION);
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
        }


        try (FileWriter fw = new FileWriter(filePath, true)) {
            if (needsNewLine) {
                fw.write(System.lineSeparator());
            }
            fw.write(data);
            fw.write(System.lineSeparator());
        }
    }

    private void overwriteFileWithLines(List<String> lines) throws IOException {
        try (FileWriter fw = new FileWriter(PurchaseRequisitionController.PR_ITEMS_FILE_PATH, false)) {
            for (String line : lines) {
                fw.write(line);
                fw.write(System.lineSeparator());
            }
        }
    }

    private static int getNextIdNumber(List<String> allPrHeaders) {
        int nextIdNumber = 1;
        if (allPrHeaders != null && !allPrHeaders.isEmpty()) {
            int maxId = 0;
            for (String line : allPrHeaders) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].startsWith("PR")) {
                    try {
                        maxId = Math.max(maxId, Integer.parseInt(parts[0].substring(2)));
                    } catch (NumberFormatException e) { /* ignore malformed IDs */ }
                }
            }
            nextIdNumber = maxId + 1;
        }
        return nextIdNumber;
    }

    @Override
    public void add(PurchaseRequisition purchaseRequisition) {
        if (purchaseRequisition == null || purchaseRequisition.getPrId() == null) {
            throw new IllegalArgumentException("Purchase Requisition or its ID cannot be null for add operation.");
        }

        try {
            // 1. PR Header
            String headerData = purchaseRequisition.toCSV();
            appendLineToFile(PR_HEADER_FILE_PATH, headerData);
            System.out.println("PR Header added: " + purchaseRequisition.getPrId());

            // 2. PR Items
            if (purchaseRequisition.getItems() != null) {
                for (PurchaseRequisitionItem item : purchaseRequisition.getItems()) {
                    item.setPrId(purchaseRequisition.getPrId());
                    appendLineToFile(PR_ITEMS_FILE_PATH, item.toCSV());
                }
                System.out.println(purchaseRequisition.getItems().size() + " item(s) added for PR: " + purchaseRequisition.getPrId());
            }
        } catch (Exception e) {
            System.out.println("Error adding purchase requisition (ID: " + purchaseRequisition.getPrId() + ") to file: " + e.getMessage());
        }
    }

    public PurchaseRequisition createNewPurchaseRequisition(String notes, String supplierId, int status,
                                                            List<PurchaseRequisitionItem> itemsData) {
        String prId;
        try {
            List<String> allPrHeaders = super.getAll(PR_HEADER_FILE_PATH);
            int nextIdNumber = getNextIdNumber(allPrHeaders);
            prId = "PR" + String.format("%03d", nextIdNumber);
        } catch (Exception e) {
            System.err.println("Error generating PR ID: " + e.getMessage());
            throw new RuntimeException("Could not generate PR ID", e);
        }

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();

        PurchaseRequisition newPR = new PurchaseRequisition(
                prId, notes, supplierId, status,
                createdAt, createdBy, createdAt, createdBy
        );

        if (itemsData != null) {
            for (PurchaseRequisitionItem item : itemsData) {
                newPR.addItem(new PurchaseRequisitionItem(null, item.getItemId(), item.getItemCode(), item.getItemName(), item.getQuantity(), item.getPrice()));
            }
        }
        add(newPR);
        System.out.println("New Purchase Requisition " + prId + " created successfully with " + newPR.getItems().size() + " items.");
        return newPR;
    }

    @Override
    public void update(PurchaseRequisition purchaseRequisition) {
        if (purchaseRequisition == null || purchaseRequisition.getPrId() == null) {
            throw new IllegalArgumentException("Purchase Requisition or its ID cannot be null for update operation.");
        }
        try {
            // 1. PR Header
            super.fileController.updateFile(purchaseRequisition.toCSV());
            System.out.println("PR Header updated: " + purchaseRequisition.getPrId());

            // 2. PR Items
            List<String> itemsToKeep = getItemsToKeep(purchaseRequisition);
            overwriteFileWithLines(itemsToKeep);
            System.out.println("Old items processed for PR: " + purchaseRequisition.getPrId());

            if (purchaseRequisition.getItems() != null) {
                for (PurchaseRequisitionItem item : purchaseRequisition.getItems()) {
                    item.setPrId(purchaseRequisition.getPrId());
                    appendLineToFile(PR_ITEMS_FILE_PATH, item.toCSV());
                }
                System.out.println(purchaseRequisition.getItems().size() + " new/updated item(s) added for PR: " + purchaseRequisition.getPrId());
            }


            System.out.println("Purchase requisition updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating full purchase requisition (ID: " + purchaseRequisition.getPrId() + ") in file: " + e.getMessage());
        }
    }

    private List<String> getItemsToKeep(PurchaseRequisition purchaseRequisition) {
        List<String> allItemLines = super.getAll(PR_ITEMS_FILE_PATH);
        List<String> itemsToKeep = new ArrayList<>();
        if (allItemLines != null) {
            for (String itemLine : allItemLines) {
                String[] parts = itemLine.split(",");
                if (parts.length > 0 && !parts[0].equals(purchaseRequisition.getPrId())) {
                    itemsToKeep.add(itemLine);
                }
            }
        }
        return itemsToKeep;
    }

    public void updatePurchaseRequisition(String prId, String notes, String supplierId, int status,
                                          List<PurchaseRequisitionItem> itemsData) {
        PurchaseRequisition existingPRHeader = getPurchaseRequisitionHeaderById(prId);
        if (existingPRHeader == null) {
            throw new IllegalArgumentException("Purchase Requisition ID " + prId + " not found for update.");
        }

        String updatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String updatedBy = SessionController.getInstance().getUserId();

        PurchaseRequisition updatedPR = new PurchaseRequisition(
                prId, notes, supplierId, status,
                existingPRHeader.getCreatedAt(), existingPRHeader.getCreatedBy(),
                updatedAt, updatedBy
        );

        if (itemsData != null) {
            for (PurchaseRequisitionItem itemDto : itemsData) {
                updatedPR.addItem(new PurchaseRequisitionItem(prId, itemDto.getItemId(), itemDto.getItemCode(), itemDto.getItemName(), itemDto.getQuantity(), itemDto.getPrice()));
            }
        }
        update(updatedPR);
        System.out.println("Purchase Requisition " + prId + " updated successfully.");
    }

    public PurchaseRequisition getPurchaseRequisitionHeaderById(String prId) {
        try {
            new FileController(PR_HEADER_FILE_PATH);
            String[] headerParts = super.fileController.getLine(0, prId);
            if (headerParts != null) {
                return PurchaseRequisition.fromHeaderCSV(String.join(",", headerParts));
            }
        } catch (IOException e) {
            System.err.println("Error reading PR header for ID " + prId + ": " + e.getMessage());
        }
        return null;
    }

    public PurchaseRequisition getFullPurchaseRequisitionById(String prId) {
        PurchaseRequisition prHeader = getPurchaseRequisitionHeaderById(prId);
        if (prHeader == null) {
            return null;
        }

        List<PurchaseRequisitionItem> prItems = new ArrayList<>();
        try {
            List<String> itemLines = super.getAll(PR_ITEMS_FILE_PATH);
            if (itemLines != null) {
                for (String line : itemLines) {
                    String[] parts = line.split(",");
                    if (parts.length > 0 && parts[0].equals(prId)) {
                        PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(line);
                        if (item != null) {
                            prItems.add(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading PR items for PR ID " + prId + ": " + e.getMessage());
        }
        prHeader.setItems(prItems);
        return prHeader;
    }

    public List<PurchaseRequisition> getAllPurchaseRequisitionHeaders() {
        List<PurchaseRequisition> prHeaders = new ArrayList<>();
        try {
            List<String> lines = super.getAll(PR_HEADER_FILE_PATH);
            if (lines != null) {
                for (String line : lines) {
                    PurchaseRequisition pr = PurchaseRequisition.fromHeaderCSV(line);
                    if (pr != null) {
                        prHeaders.add(pr);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading all PR headers: " + e.getMessage());
        }
        return prHeaders;
    }

    public List<PurchaseRequisition> getAllFullPurchaseRequisitions() {
        List<PurchaseRequisition> fullPRs = new ArrayList<>();
        List<PurchaseRequisition> prHeaders = getAllPurchaseRequisitionHeaders();

        Map<String, List<PurchaseRequisitionItem>> allItemsMap = new HashMap<>();
        try {
            List<String> itemLines = super.getAll(PR_ITEMS_FILE_PATH);
            if (itemLines != null) {
                for (String line : itemLines) {
                    PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(line);
                    if (item != null && item.getPrId() != null) {
                        allItemsMap.computeIfAbsent(item.getPrId(), _ -> new ArrayList<>()).add(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading all PR items: " + e.getMessage());
        }

        for (PurchaseRequisition header : prHeaders) {
            header.setItems(allItemsMap.getOrDefault(header.getPrId(), new ArrayList<>()));
            fullPRs.add(header);
        }
        return fullPRs;
    }

    @Override
    public void delete(String prId) {
        if (prId == null || prId.trim().isEmpty()) {
            throw new IllegalArgumentException("PR ID cannot be null or empty for delete operation.");
        }
        try {
            // 1. Delete PR Header
            new FileController(PR_HEADER_FILE_PATH);
            super.fileController.deleteLine(prId, 0);

            // 2. Delete PR Items
            List<String> allItemLines = super.getAll(PR_ITEMS_FILE_PATH);
            List<String> itemsToKeep = new ArrayList<>();
            boolean itemsModified = false;
            if (allItemLines != null) {
                for (String itemLine : allItemLines) {
                    String[] parts = itemLine.split(",");
                    if (parts.length > 0 && parts[0].equals(prId)) {
                        itemsModified = true;
                    } else {
                        itemsToKeep.add(itemLine);
                    }
                }
            }

            if (itemsModified) {
                overwriteFileWithLines(itemsToKeep);
                System.out.println("Items associated with PR " + prId + " deleted.");
            } else {
                System.out.println("No items found for PR " + prId + " or no changes needed to items file.");
            }

        } catch (IOException e) {
            System.err.println("Error deleting purchase requisition (ID: " + prId + "): " + e.getMessage());
            throw new RuntimeException("Failed to delete purchase requisition: " + prId, e);
        } catch (Exception e) {
            System.err.println("Unexpected error during deletion of PR (ID: " + prId + "): " + e.getMessage());
            throw new RuntimeException("Failed to delete purchase requisition: " + prId, e);
        }
    }
}
