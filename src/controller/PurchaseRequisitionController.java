package controller;

import model.PurchaseRequisition;
import model.EntityType;
import model.PurchaseRequisitionItem;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PurchaseRequisitionController extends CRUDController<PurchaseRequisition> {
    private static final String PR_ITEMS_FILE_PATH = "data/purchase_requisition_item.txt";
    private static final String PR_HEADER_FILE_PATH = "data/purchase_requisition.txt"; // This is super.filePath

    public PurchaseRequisitionController() {
        super(PR_HEADER_FILE_PATH, EntityType.PURCHASE_REQUISITION);
    }

    public String getPrFilePath() {
        return PR_ITEMS_FILE_PATH;
    }

    public String getPrHeaderFilePath() {
        return PR_HEADER_FILE_PATH;
    }


    // appendLineToFile and overwriteFileWithLines seem to use FileWriter directly,
    // which is independent of the FileController static path issue, so they should be okay.
    // Ensure these methods correctly handle file creation if not exists and newlines.
    private void appendLineToFile(String filePath, String data) throws IOException {
        // Ensure FileController path is set if any static FileController methods are used INTERNALLY by this,
        // OR ensure this method is fully self-contained. Current implementation uses FileWriter directly.
        // For robustness with static FileController, one might consider:
        // FileController localFC = new FileController(filePath); // Sets static path if constructor does
        // localFC.appendLine(data); // If appendLine becomes non-static and safe

        // Original direct FileWriter logic (generally fine for specific file operations):
        boolean needsNewLine = false;
        File file = new File(filePath);
        if (!file.exists()) {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            file.createNewFile();
        } else if (file.length() > 0) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                if (raf.length() > 0) {
                    raf.seek(raf.length() - 1);
                    if (raf.read() != '\n') {
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

    private void overwriteFileWithLines(String filePath, List<String> lines) throws IOException {
        // Similar to appendLineToFile, ensure no problematic static FileController calls if any were added.
        // Original direct FileWriter logic (generally fine):
        try (FileWriter fw = new FileWriter(filePath, false)) { // false to overwrite
            for (int i = 0; i < lines.size(); i++) {
                fw.write(lines.get(i));
                if (i < lines.size() -1 ) { // Only add newline if it's not the last line
                    fw.write(System.lineSeparator());
                } else if (!lines.get(i).endsWith(System.lineSeparator()) && !lines.get(i).isEmpty()){
                    // Ensure last line has a newline if it's not empty and doesn't have one
                    // This behavior might need adjustment based on how readAllLines handles last empty lines.
                    // For now, let's stick to adding newlines between lines.
                    // A common practice is for each line written (including the last) to end with a newline.
                    // The appendLineToFile method already does this.
                    // Let's adjust this to ensure all non-empty lines end with a newline.
                }
            }
        }
        // Let's refine overwriteFileWithLines for consistent newline handling:
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }


    private static int getNextIdNumber(List<String> allPrHeaders) {
        if (allPrHeaders == null || allPrHeaders.isEmpty()) {
            return 1;
        }
        int maxId = 0;
        for (String line : allPrHeaders) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length > 0 && parts[0].startsWith("PR")) {
                try {
                    maxId = Math.max(maxId, Integer.parseInt(parts[0].substring(2)));
                } catch (NumberFormatException e) { /* ignore malformed IDs */ }
            }
        }
        return maxId + 1;
    }

    @Override
    public void add(PurchaseRequisition purchaseRequisition) {
        if (purchaseRequisition == null || purchaseRequisition.getPrId() == null) {
            throw new IllegalArgumentException("Purchase Requisition or its ID cannot be null for add operation.");
        }

        try {
            // 1. PR Header - Uses this.filePath (PR_HEADER_FILE_PATH)
            // The toCSV() in PurchaseRequisition model is already updated (no supplierId)
            new FileController(this.filePath); // Ensure correct static path for super.fileController operations
            String headerData = purchaseRequisition.toCSV();
            // super.fileController.appendLine(headerData); // If using CRUDController's file ops
            appendLineToFile(this.filePath, headerData); // Or using local direct write
            System.out.println("PR Header added: " + purchaseRequisition.getPrId());

            // 2. PR Items - Uses PR_ITEMS_FILE_PATH
            // The toCSV() in PurchaseRequisitionItem model is updated (includes suggestedSupplierIds)
            if (purchaseRequisition.getItems() != null) {
                for (PurchaseRequisitionItem item : purchaseRequisition.getItems()) {
                    if (item == null) continue;
                    item.setPrId(purchaseRequisition.getPrId()); // Ensure PR ID is set
                    appendLineToFile(PR_ITEMS_FILE_PATH, item.toCSV());
                }
                System.out.println(purchaseRequisition.getItems().size() + " item(s) added for PR: " + purchaseRequisition.getPrId());
            }
        } catch (IOException e) { // Changed from Exception to IOException
            System.err.println("Error adding purchase requisition (ID: " + purchaseRequisition.getPrId() + ") to file: " + e.getMessage());
            e.printStackTrace(); // Good for debugging
            // Consider re-throwing as a custom runtime exception or handling more gracefully
        }
    }

    // supplierId parameter removed from signature
    public PurchaseRequisition createNewPurchaseRequisition(String notes, int status,
                                                            List<PurchaseRequisitionItem> itemsData) {
        String prId;
        try {
            // getAll() from CRUDController now handles its own static path setting
            List<String> allPrHeaders = getAll(); // Reads PR_HEADER_FILE_PATH
            int nextIdNumber = getNextIdNumber(allPrHeaders);
            prId = "PR" + String.format("%03d", nextIdNumber);
        } catch (Exception e) { // Catch general exception from getAll or getNextIdNumber
            System.err.println("Error generating PR ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not generate PR ID", e); // Or return null
        }

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();
        if (createdBy == null) createdBy = "system"; // Fallback

        // Create PurchaseRequisition without supplierId
        PurchaseRequisition newPR = new PurchaseRequisition(
                prId, notes, status,
                createdAt, createdBy, createdAt, createdBy // Assuming updatedAt/By are same initially
        );

        if (itemsData != null) {
            for (PurchaseRequisitionItem itemDto : itemsData) {
                if (itemDto == null) continue;
                // The itemDto from AddPurchaseRequisitionForm should already have its suggestedSupplierIds set.
                // We just need to ensure its PR ID is linked when adding to the PR object.
                newPR.addItem(new PurchaseRequisitionItem(
                        null, // PR ID will be set by newPR.addItem()
                        itemDto.getItemId(),
                        itemDto.getItemCode(),
                        itemDto.getItemName(),
                        itemDto.getQuantity(),
                        itemDto.getPrice(), // Assuming price is already in correct unit (e.g., cents)
                        itemDto.getSuggestedSupplierIds() // Pass the list
                ));
            }
        }
        add(newPR); // Calls the overridden add method
        System.out.println("New Purchase Requisition " + prId + " created successfully with " + newPR.getItems().size() + " items.");
        return newPR;
    }

    @Override
    public void update(PurchaseRequisition purchaseRequisition) {
        if (purchaseRequisition == null || purchaseRequisition.getPrId() == null) {
            throw new IllegalArgumentException("Purchase Requisition or its ID cannot be null for update operation.");
        }
        try {
            // 1. Update PR Header
            // The toCSV() in PurchaseRequisition model is updated (no supplierId)
            new FileController(this.filePath); // Ensure correct static path for super.fileController operations
            super.fileController.updateFile(purchaseRequisition.toCSV()); // Assuming ID is at index 0
            System.out.println("PR Header updated: " + purchaseRequisition.getPrId());

            // 2. Update PR Items: Delete all old items for this PR and add new ones
            List<String> allCurrentItemLines = readAllLinesFromFile(PR_ITEMS_FILE_PATH);
            List<String> itemsToKeep = new ArrayList<>();
            if (allCurrentItemLines != null) {
                for (String itemLine : allCurrentItemLines) {
                    String[] parts = itemLine.split(",", -1);
                    if (parts.length > 0 && !parts[0].equals(purchaseRequisition.getPrId())) {
                        itemsToKeep.add(itemLine); // Keep items from other PRs
                    }
                }
            }
            // Add the updated items for the current PR
            if (purchaseRequisition.getItems() != null) {
                for (PurchaseRequisitionItem item : purchaseRequisition.getItems()) {
                    if (item == null) continue;
                    item.setPrId(purchaseRequisition.getPrId()); // Ensure PR ID
                    itemsToKeep.add(item.toCSV()); // Add updated/new items
                }
            }
            overwriteFileWithLines(PR_ITEMS_FILE_PATH, itemsToKeep);
            System.out.println("Items updated for PR: " + purchaseRequisition.getPrId());

        } catch (IOException e) { // Changed from Exception to IOException
            System.err.println("Error updating full purchase requisition (ID: " + purchaseRequisition.getPrId() + ") in file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // supplierId parameter removed
    public void updatePurchaseRequisition(String prId, String notes, int status,
                                          List<PurchaseRequisitionItem> itemsData) {
        // getPurchaseRequisitionHeaderById uses PurchaseRequisition.fromHeaderCSV (updated)
        PurchaseRequisition existingPRHeader = getPurchaseRequisitionHeaderById(prId);
        if (existingPRHeader == null) {
            throw new IllegalArgumentException("Purchase Requisition ID " + prId + " not found for update.");
        }

        String updatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String updatedBy = SessionController.getInstance().getUserId();
        if (updatedBy == null) updatedBy = "system";

        // Create updated PR object without supplierId
        PurchaseRequisition updatedPR = new PurchaseRequisition(
                prId, notes, status,
                existingPRHeader.getCreatedAt(), existingPRHeader.getCreatedBy(),
                updatedAt, updatedBy
        );

        if (itemsData != null) {
            for (PurchaseRequisitionItem itemDto : itemsData) {
                if (itemDto == null) continue;
                // The itemDto from AddPurchaseRequisitionForm should have its suggestedSupplierIds
                updatedPR.addItem(new PurchaseRequisitionItem(
                        null, // PR ID set by updatedPR.addItem()
                        itemDto.getItemId(), itemDto.getItemCode(), itemDto.getItemName(),
                        itemDto.getQuantity(), itemDto.getPrice(),
                        itemDto.getSuggestedSupplierIds() // Pass the list
                ));
            }
        }
        update(updatedPR); // Calls the overridden update method
        System.out.println("Purchase Requisition " + prId + " updated successfully.");
    }

    public PurchaseRequisition getPurchaseRequisitionHeaderById(String prId) {
        // This method calls super.getOneWithId(), which uses PurchaseRequisition.fromHeaderCSV().
        // PurchaseRequisition.fromHeaderCSV() is already updated for 7 fields.
        String headerLine = super.getOneWithId(prId); // Relies on CRUDController.getOneWithId path setting
        if (headerLine != null) {
            return PurchaseRequisition.fromHeaderCSV(headerLine);
        }
        return null;
    }

    private List<String> readAllLinesFromFile(String filePath) throws IOException {
        // This helper method is used to read item lines.
        // It needs to use the static FileController carefully.
        new FileController(filePath); // Set static path for the target file
        return FileController.getFile(); // Use static method
    }


    public PurchaseRequisition getFullPurchaseRequisitionById(String prId) {
        PurchaseRequisition prHeader = getPurchaseRequisitionHeaderById(prId);
        if (prHeader == null) {
            return null;
        }

        List<PurchaseRequisitionItem> prItems = new ArrayList<>();
        try {
            List<String> itemLines = readAllLinesFromFile(PR_ITEMS_FILE_PATH); // Uses helper that sets path
            if (itemLines != null) {
                for (String line : itemLines) {
                    if (line == null || line.trim().isEmpty()) continue;
                    String[] parts = line.split(",", -1);
                    // Check if the item line belongs to the current PR
                    if (parts.length > 0 && parts[0].equals(prId)) {
                        // PurchaseRequisitionItem.fromCSV() is already updated for 8 fields
                        PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(line);
                        if (item != null) {
                            prItems.add(item);
                        }
                    }
                }
            }
        } catch (IOException e) { // Changed from Exception
            System.err.println("Error reading PR items for PR ID " + prId + ": " + e.getMessage());
            e.printStackTrace();
        }
        prHeader.setItems(prItems);
        return prHeader;
    }

    public List<PurchaseRequisition> getAllPurchaseRequisitionHeaders() {
        // super.getAll() already calls new FileController(this.filePath)
        List<String> lines = super.getAll(); // Reads PR_HEADER_FILE_PATH
        List<PurchaseRequisition> prHeaders = new ArrayList<>();
        if (lines != null) {
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) continue;
                // PurchaseRequisition.fromHeaderCSV() is updated
                PurchaseRequisition pr = PurchaseRequisition.fromHeaderCSV(line);
                if (pr != null) {
                    prHeaders.add(pr);
                }
            }
        }
        return prHeaders;
    }

    public List<PurchaseRequisition> getAllFullPurchaseRequisitions() {
        List<PurchaseRequisition> prHeaders = getAllPurchaseRequisitionHeaders();
        List<PurchaseRequisition> fullPRs = new ArrayList<>();

        // Efficiently read all item lines once
        Map<String, List<PurchaseRequisitionItem>> allItemsMap = new HashMap<>();
        try {
            List<String> itemLines = readAllLinesFromFile(PR_ITEMS_FILE_PATH); // Uses helper that sets path
            if (itemLines != null) {
                for (String line : itemLines) {
                    if (line == null || line.trim().isEmpty()) continue;
                    // PurchaseRequisitionItem.fromCSV() is updated
                    PurchaseRequisitionItem item = PurchaseRequisitionItem.fromCSV(line);
                    if (item != null && item.getPrId() != null) {
                        allItemsMap.computeIfAbsent(item.getPrId(), k -> new ArrayList<>()).add(item);
                    }
                }
            }
        } catch (IOException e) { // Changed from Exception
            System.err.println("Error reading all PR items: " + e.getMessage());
            e.printStackTrace();
        }

        for (PurchaseRequisition header : prHeaders) {
            if (header == null) continue;
            // It's better to create a new PR object for full PRs to avoid modifying the header objects from the list
            PurchaseRequisition fullPR = PurchaseRequisition.fromHeaderCSV(header.toCSV()); // Re-parse to get a fresh object
            if (fullPR != null) {
                fullPR.setItems(allItemsMap.getOrDefault(header.getPrId(), new ArrayList<>()));
                fullPRs.add(fullPR);
            }
        }
        return fullPRs;
    }

    @Override
    public void delete(String prId) {
        if (prId == null || prId.trim().isEmpty()) {
            throw new IllegalArgumentException("PR ID cannot be null or empty for delete operation.");
        }
        try {
            // 1. Delete PR Header using CRUDController's delete, which sets its own path
            super.delete(prId); // Deletes from PR_HEADER_FILE_PATH

            // 2. Delete associated PR Items from PR_ITEMS_FILE_PATH
            List<String> allItemLines = readAllLinesFromFile(PR_ITEMS_FILE_PATH); // Helper sets path
            List<String> itemsToKeep = new ArrayList<>();
            boolean itemsModified = false;
            if (allItemLines != null) {
                for (String itemLine : allItemLines) {
                    if (itemLine == null || itemLine.trim().isEmpty()) continue;
                    String[] parts = itemLine.split(",", -1);
                    if (parts.length > 0 && parts[0].equals(prId)) {
                        itemsModified = true; // This line is for the PR being deleted
                    } else {
                        itemsToKeep.add(itemLine); // Keep lines from other PRs
                    }
                }
            }

            if (itemsModified) {
                overwriteFileWithLines(PR_ITEMS_FILE_PATH, itemsToKeep);
                System.out.println("Items associated with PR " + prId + " deleted.");
            } else {
                System.out.println("No items found for PR " + prId + " or no changes needed to items file.");
            }

        } catch (IOException e) { // Changed from generic Exception
            System.err.println("Error deleting purchase requisition (ID: " + prId + "): " + e.getMessage());
            e.printStackTrace();
            // Consider re-throwing as a custom runtime exception or handling more gracefully
        }
    }
}