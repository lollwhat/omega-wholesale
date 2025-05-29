package controller;

import model.ItemSupplier;
import model.EntityType; // You'll need to add ITEM_SUPPLIER_LINK to your EntityType enum

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ItemSupplierController extends CRUDController<ItemSupplier> {

    public ItemSupplierController() {
        super("data/item_supplier.txt", EntityType.ITEM_SUPPLIER);
    }

    public List<String> getSupplierIdsForItem(String itemId) {
        List<String> supplierIds = new ArrayList<>();
        if (itemId == null || itemId.trim().isEmpty()) return supplierIds;
        try {
            new FileController("data/item_supplier.txt"); // Set static path
            List<String> allLinks = FileController.getFile();
            if (allLinks != null) {
                for (String line : allLinks) {
                    ItemSupplier link = ItemSupplier.fromCSV(line);
                    if (link != null && link.getItemId().equals(itemId)) {
                        supplierIds.add(link.getSupplierId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting supplier IDs for item " + itemId + ": " + e.getMessage());
        }
        return supplierIds;
    }

    public List<String> getItemIdsBySupplier(String supplierId) {
        List<String> itemIds = new ArrayList<>();
        if (supplierId == null || supplierId.trim().isEmpty()) return itemIds;
        try {
            new FileController("data/item_supplier.txt"); // Set static path
            List<String> allLinks = FileController.getFile();
            if (allLinks != null) {
                for (String line : allLinks) {
                    ItemSupplier link = ItemSupplier.fromCSV(line);
                    if (link != null && link.getSupplierId().equals(supplierId)) {
                        itemIds.add(link.getItemId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting item IDs for supplier " + supplierId + ": " + e.getMessage());
        }
        return itemIds;
    }

    private void appendLineToSpecificFile(String filePath, String data) throws IOException {
        boolean needsNewLine = false;
        File file = new File(filePath);
        if (file.exists() && file.length() > 0) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                if (raf.length() > 0) {
                    raf.seek(raf.length() - 1);
                    char lastChar = (char) raf.read();
                    if (lastChar != '\n' && lastChar != '\r') needsNewLine = true;
                }
            }
        } else if (!file.exists()){ // Should be created by constructor, but defensive
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            file.createNewFile();
        }

        try (FileWriter fw = new FileWriter(filePath, true)) {
            if (needsNewLine) fw.write(System.lineSeparator());
            fw.write(data);
            fw.write(System.lineSeparator());
        }
    }

    private void overwriteFileWithLines(String filePath, List<String> lines) throws IOException {
        try (FileWriter fw = new FileWriter(filePath, false)) { // false to overwrite
            for (int i = 0; i < lines.size(); i++) {
                fw.write(lines.get(i));
                if (i < lines.size() ) {
                    fw.write(System.lineSeparator());
                }
            }
        }
    }

    public void addLink(String itemId, String supplierId) {
        if (linkExists(itemId, supplierId)) {
            System.out.println("Link between Item " + itemId + " and Supplier " + supplierId + " already exists.");
            return;
        }
        ItemSupplier newLink = new ItemSupplier(itemId, supplierId);
        try {
            appendLineToSpecificFile("data/item_supplier.txt", newLink.toCSV());
            System.out.println("Link added: Item " + itemId + " -> Supplier " + supplierId);
        } catch (IOException e) {
            System.err.println("Error adding item-supplier link: " + e.getMessage());
        }
    }

    public void removeLink(String itemId, String supplierId) {
        if (itemId == null || supplierId == null || itemId.trim().isEmpty() || supplierId.trim().isEmpty()) {
            System.err.println("Item ID and Supplier ID cannot be empty for removing a link.");
            return;
        }
        List<String> updatedLinks = new ArrayList<>();
        boolean linkRemoved = false;
        try {
            new FileController("data/item_supplier.txt"); // Set static path for FileController.getFile()
            List<String> allLinks = FileController.getFile();
            if (allLinks != null) {
                for (String line : allLinks) {
                    ItemSupplier link = ItemSupplier.fromCSV(line);
                    if (link != null && link.getItemId().equals(itemId) && link.getSupplierId().equals(supplierId)) {
                        linkRemoved = true; // Skip this line to "delete" it
                    } else {
                        updatedLinks.add(line);
                    }
                }
            }
            if (linkRemoved) {
                overwriteFileWithLines("data/item_supplier.txt", updatedLinks);
                System.out.println("Link removed: Item " + itemId + " <-> Supplier " + supplierId);
            } else {
                System.out.println("Link not found to remove: Item " + itemId + " <-> Supplier " + supplierId);
            }
        } catch (IOException e) {
            System.err.println("Error removing item-supplier link: " + e.getMessage());
        } catch (Exception e) { // Catch RuntimeException from FileController.getFile()
            System.err.println("Unexpected error removing item-supplier link: " + e.getMessage());
        }
    }

    /**
     * Checks if a specific link between an item and a supplier exists.
     * @param itemId The item ID.
     * @param supplierId The supplier ID.
     * @return true if the link exists, false otherwise.
     */
    public boolean linkExists(String itemId, String supplierId) {
        try {
            new FileController("data/item_supplier.txt"); // Set static path
            List<String> allLinks = FileController.getFile();
            if (allLinks != null) {
                for (String line : allLinks) {
                    ItemSupplier link = ItemSupplier.fromCSV(line);
                    if (link != null && link.getItemId().equals(itemId) && link.getSupplierId().equals(supplierId)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking if link exists: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void add(ItemSupplier data) { // Used by createNewPurchaseRequisition in PRController...
        // This is the generic add. We'll use the specific addLink for clarity.
        if (data != null) {
            addLink(data.getItemId(), data.getSupplierId());
        }
    }

    @Override
    public void update(ItemSupplier data) {
        // Updating a link usually means deleting the old one and adding the new one if IDs change.
        // If other attributes were on the link, this would be more relevant.
        // For now, direct users to use removeLink then addLink if keys change.
        System.err.println("ItemSupplierController.update(ItemSupplier) is not fully implemented. Use removeLink then addLink.");
        // If you wanted to update based on a composite key, you'd need more complex logic here.
    }

    // delete(String id) from CRUDController would expect a single ID.
    // For links, deletion is better handled by removeLink(itemId, supplierId).
    // You could override delete(String compositeId) if you adopt a convention like "itemId;supplierId".
    // For now, this generic delete is not very useful for links.
    @Override
    public void delete(String id) {
        System.err.println("ItemSupplierController.delete(String id) is not recommended for links. Use removeLink(itemId, supplierId).");
    }
}