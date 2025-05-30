package controller;

import model.EntityType;
import model.Stock;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class StockController extends CRUDController<Stock> {
    private static final String STOCK_DETAILS_FILE = "data/stock_details.txt";

    public StockController() {
        super("data/stock_details.txt", EntityType.STOCK);
    }

    @Override
    public void add(Stock stock) {
        try {
            String data = stock.toString();
            FileController.appendFile(data);
            System.out.println("Stock added successfully.");
        } catch (Exception e) {
            System.err.println("Error adding stock to file: " + e.getMessage());
        }
    }

    public void addStock(String itemCode, String name, int currentStock, int minStock, int maxStock, String status, String lastUpdateDate) {
        if (currentStock < minStock || currentStock > maxStock) {
            System.err.println("Error: Current stock must be between minStock and maxStock.");
            return;
        }

        try {
            int tempStockId = FileController.getFile().size() + 1;
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String updatedBy = SessionController.getInstance().getUserId();

            Stock stock = new Stock(
                    "ST" + String.format("%03d", tempStockId),
                    itemCode,
                    name,
                    currentStock,
                    minStock,
                    maxStock,
                    status,
                    lastUpdateDate
            );

            add(stock);
        } catch (Exception e) {
            System.err.println("Error adding stock: " + e.getMessage());
        }
    }

    @Override
    public void update(Stock stock) {
        try {
            String data = stock.toString();
            fileController.updateFile(data);
            System.out.println("Stock updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating stock: " + e.getMessage());
        }
    }

    public void updateStock(String stockId, String name, Integer currentStock, Integer minStock, Integer maxStock, String status) {
        try {
            String[] existingStockDetails = fileController.getLine(0, stockId);
            if (existingStockDetails == null) {
                System.err.println("Error: Stock not found.");
                return;
            }

            Stock stock = new Stock(
                    stockId,
                    existingStockDetails[1],
                    existingStockDetails[2],
                    Integer.parseInt(existingStockDetails[3]),
                    Integer.parseInt(existingStockDetails[4]),
                    Integer.parseInt(existingStockDetails[5]),
                    existingStockDetails[6],
                    existingStockDetails[7]
            );

            if (currentStock != null && (currentStock < minStock || currentStock > maxStock)) {
                System.err.println("Error: Current stock must be between minStock and maxStock.");
                return;
            }

            stock.setItemCode(existingStockDetails[1]);
            stock.setName((name != null) ? name : existingStockDetails[2]);
            stock.setCurrentStock((currentStock != null) ? currentStock : Integer.parseInt(existingStockDetails[3]));
            stock.setMinStock((minStock != null) ? minStock : Integer.parseInt(existingStockDetails[4]));
            stock.setMaxStock((maxStock != null) ? maxStock : Integer.parseInt(existingStockDetails[5]));
            stock.setStatus((status != null) ? status : existingStockDetails[6]);
            stock.setLastUpdateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            update(stock);
        } catch (Exception e) {
            System.err.println("Error updating stock: " + e.getMessage());
        }
    }

    public boolean subtractStockQuantity(String itemCode, int quantityToSubtract) {
        if (quantityToSubtract <= 0) {
            System.err.println("Error: Quantity to subtract must be positive.");
            return false;
        }

        try {
            List<String> stockLines = FileController.getFile(); // Reads from this.filePath which is STOCK_DETAILS_FILE
            List<String> updatedStockLines = new ArrayList<>();
            boolean itemFoundAndUpdated = false;
            String stockFilePath = this.filePath; // Use the filePath from CRUDController

            for (String line : stockLines) {
                String[] stockDetails = line.split(",");
                String currentItemCode = stockDetails[1].trim();

                if (currentItemCode.equalsIgnoreCase(itemCode)) {
                    int currentStock = Integer.parseInt(stockDetails[3].trim());
                    int minStock = Integer.parseInt(stockDetails[4].trim());
                    // String currentStatus = stockDetails[6].trim(); // Current status

                    if (currentStock < quantityToSubtract) {
                        System.err.println("Error: Insufficient stock for item " + itemCode +
                                ". Available: " + currentStock + ", Requested: " + quantityToSubtract);
                        return false; // Indicate failure
                    }

                    int newStock = currentStock - quantityToSubtract;
                    stockDetails[3] = String.valueOf(newStock); // Update quantity
                    stockDetails[7] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // Update lastUpdateDate

                    // Update status string based on new stock level
                    if (newStock == 0) {
                        stockDetails[6] = "Out of Stock";
                    } else if (newStock <= minStock) {
                        stockDetails[6] = "Low Stock";
                    } else {
                        stockDetails[6] = "In Stock";
                    }
                    itemFoundAndUpdated = true;
                }
                updatedStockLines.add(String.join(",", stockDetails));
            }

            if (!itemFoundAndUpdated) {
                System.err.println("Error: Item code " + itemCode + " not found in stock for subtraction.");
                return false;
            }

            // Overwrite the file with updated stock lines
            Files.write(Paths.get(stockFilePath), updatedStockLines);
            System.out.println("Stock quantity subtracted successfully for item: " + itemCode);
            return true;

        } catch (IOException e) {
            System.err.println("Error reading/writing stock file during subtraction: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing stock quantity from file: " + e.getMessage());
            return false;
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("An unexpected error occurred during stock subtraction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // mark purchase orders as received
    public boolean markPurchaseOrderAsReceived(String poId) {
        try {
            List<String> poLines = Files.readAllLines(Paths.get("data/purchase_order.txt"));
            List<String> poItemLines = Files.readAllLines(Paths.get("data/purchase_order_item.txt"));
            List<String> stockLines = Files.readAllLines(Paths.get("data/stock_details.txt"));

            boolean poFound = false;

            // validate the PO in purchase_order.txt
            for (int i = 0; i < poLines.size(); i++) {
                String[] poDetails = poLines.get(i).split(",");
                for (int k = 0; k < poDetails.length; k++) {
                    poDetails[k] = poDetails[k].trim();
                }
                if (poDetails[0].equals(poId)) {
                    poFound = true;

                    if (!"1".equals(poDetails[4].trim())) { // check if PO is not "Approved"
                        System.out.println("Purchase order ID " + poId + " is not in 'Approved' state and cannot be received.");
                        return false;
                    }

                    poDetails[4] = "2";
                    poDetails[7] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    poDetails[8] = SessionController.getInstance().getUserId();
                    poDetails[9] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    poDetails[10] = SessionController.getInstance().getUserId();
                    poLines.set(i, String.join(",", poDetails));
                    break;
                }
            }

            if (!poFound) {
                System.out.println("Purchase order ID " + poId + " not found.");
                return false;
            }

            // update stock from purchase_order_item.txt
            List<String> updatedStockLines = new ArrayList<>(stockLines);

            for (String poItemLine : poItemLines) {
                String[] poItemDetails = poItemLine.split(",");
                for (int k = 0; k < poItemDetails.length; k++) {
                    poItemDetails[k] = poItemDetails[k].trim();
                }

                if (poItemDetails[0].equals(poId)) { // Check if the item belongs to the current PO
                    String itemCodeFromPO = poItemDetails[2]; // PurchaseOrderItem.itemCode (index 2)
                    int quantityToAdd = Integer.parseInt(poItemDetails[4]); // PurchaseOrderItem.quantity (index 4)

                    boolean itemStockUpdated = false;
                    for (int j = 0; j < updatedStockLines.size(); j++) {
                        String[] stockDetails = updatedStockLines.get(j).split(",");
                        for (int k = 0; k < stockDetails.length; k++) {
                            stockDetails[k] = stockDetails[k].trim();
                        }

                        // Match using itemCode (stockDetails[1] is Stock.itemCode)
                        if (stockDetails[1].equalsIgnoreCase(itemCodeFromPO)) {
                            int currentStockValue = Integer.parseInt(stockDetails[3].trim()); // currentStock is at index 3
                            int maxStockValue = Integer.parseInt(stockDetails[5].trim()); // maxStock is at index 5

                            int newStock = currentStockValue + quantityToAdd;
                            // Optional: Check against maxStock if necessary, though typically receiving POs increases stock.
                            // if (newStock > maxStockValue) {
                            //     System.err.println("Warning: Receiving item " + itemCodeFromPO + " exceeds max stock level.");
                            //     // Decide handling: cap at maxStock or allow exceeding
                            // }

                            stockDetails[3] = String.valueOf(newStock); // Update currentStock at index 3
                            stockDetails[7] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // Update lastUpdateDate at index 7
                            updatedStockLines.set(j, String.join(",", stockDetails));
                            itemStockUpdated = true;
                            break;
                        }
                    }

                    // If the item was not found in existing stock_details.txt to update,
                    // the current logic for 'newStockEntry' might need review as well,
                    // especially regarding default values and ensuring itemCode is used consistently.
                    // The provided code always adds a new entry regardless of itemStockUpdated, which might be unintentional.
                    // if (!itemStockUpdated) { // Logic to add new stock if not found
                    //     System.out.println("Item " + itemCodeFromPO + " not found in stock, adding as new entry.");
                    //     // Ensure the format matches Stock.java (id, itemCode, name, currentStock, minStock, maxStock, status, lastUpdateDate)
                    //     // poItemDetails[3] is itemName
                    //     String newStockEntry = String.format(
                    //             "ST%03d,%s,%s,%d,%d,%d,%s,%s", // Assuming STxxx is the ID format
                    //             updatedStockLines.size() + stockLines.size() +1, // This ID generation might need to be more robust
                    //             itemCodeFromPO, // itemCode
                    //             poItemDetails[3], // itemName
                    //             quantityToAdd, // currentStock
                    //             0, // Default minStock
                    //             100, // Default maxStock (example)
                    //             "In Stock", // Default status
                    //             new SimpleDateFormat("yyyy-MM-dd").format(new Date()) // lastUpdateDate
                    //     );
                    //     updatedStockLines.add(newStockEntry);
                    // }
                    // The current code for newStockEntry is outside the if(poItemDetails[0].equals(poId)) block in the provided snippet,
                    // and has 'itemName' where 'itemCode' might be expected for the second field if it follows Stock.java strictly.
                    // The provided code has:
                    // String newStockEntry = String.format(
                    // "ST%03d,%s,%d,DEFAULT_UNIT,DEFAULT_REORDER_LVL,DEFAULT_REORDER_QTY,In Stock,%s", ... itemName, quantityToAdd ...
                    // This format doesn't match the Stock object structure (needs itemCode, name, current, min, max, status, date).
                    // This part needs careful review to ensure new stock entries are correct.
                }
            }

            // write back updated data to files
            Files.write(Paths.get("data/purchase_order.txt"), poLines);
            Files.write(Paths.get("data/stock_details.txt"), updatedStockLines);

            System.out.println("Purchase order marked as received: " + poId);
            return true;

        } catch (IOException e) {
            System.err.println("Error processing purchase order: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values: " + e.getMessage());
            return false;
        }
    }

    public void deleteStock(String stockId) {
        try {
            fileController.deleteLine(stockId, 0);
            System.out.println("Stock deleted successfully.");
        } catch (Exception e) {
            System.err.println("Error deleting stock: " + e.getMessage());
        }
    }

    public List<String> getAllStocks() {
        new FileController(this.filePath);
        try {
            return FileController.getFile();
        } catch (Exception e) {
            System.err.println("Error reading stocks from file: " + e.getMessage());
            return null;
        }
    }

    public String getStockById(String stockId) {
        try {
            String[] stockDetails = fileController.getLine(0, stockId);
            if (stockDetails != null) {
                return String.join(",", stockDetails);
            } else {
                System.err.println("Error: Stock not found.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error reading stock: " + e.getMessage());
            return null;
        }
    }

    public String getStockByItemCode(String itemCode) {
        try {
            String[] stockDetails = fileController.getLine(1, itemCode);
            if (stockDetails != null) {
                return String.join(",", stockDetails);
            } else {
                System.err.println("Error: Stock not found.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error reading stock: " + e.getMessage());
            return null;
        }
    }

    public void generateStockReport(String savePath) throws IOException {
        Path stockFilePath = Paths.get(STOCK_DETAILS_FILE);

        if (!Files.exists(stockFilePath)) {
            throw new IOException("Stock file not found.");
        }

        // read the stock data
        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Stock ID,Item Code, Item Name,Current Stock,Min Stock,Max Stock,Status,Date\n");
        for (String line : Files.readAllLines(stockFilePath)) {
            reportContent.append(line).append("\n");
        }

        // save the report to the specified path
        try (FileWriter writer = new FileWriter(savePath)) {
            writer.write(reportContent.toString());
        }
    }
}