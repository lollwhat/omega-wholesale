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

    public void addStock(String name, int currentStock, int minStock, int maxStock, String status, String lastUpdateDate) {
        if (currentStock < minStock || currentStock > maxStock) {
            System.err.println("Error: Current stock must be between minStock and maxStock.");
            return;
        }

        try {
            int tempStockId = FileController.getFile().size() + 1;
            String createdAt = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String updatedBy = SessionController.getInstance().getUserId();

            Stock stock = new Stock(
                    "ST" + String.format("%03d", tempStockId),
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
                    Integer.parseInt(existingStockDetails[2]),
                    Integer.parseInt(existingStockDetails[3]),
                    Integer.parseInt(existingStockDetails[4]),
                    existingStockDetails[5],
                    existingStockDetails[6]
            );

            if (currentStock != null && (currentStock < minStock || currentStock > maxStock)) {
                System.err.println("Error: Current stock must be between minStock and maxStock.");
                return;
            }

            stock.setName((name != null) ? name : existingStockDetails[1]);
            stock.setCurrentStock((currentStock != null) ? currentStock : Integer.parseInt(existingStockDetails[2]));
            stock.setMinStock((minStock != null) ? minStock : Integer.parseInt(existingStockDetails[3]));
            stock.setMaxStock((maxStock != null) ? maxStock : Integer.parseInt(existingStockDetails[4]));
            stock.setStatus((status != null) ? status : existingStockDetails[5]);
            stock.setLastUpdateDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            update(stock);
        } catch (Exception e) {
            System.err.println("Error updating stock: " + e.getMessage());
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

                    // update status to "Received" (2)
                    poDetails[4] = "2";
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

                if (poItemDetails[0].equals(poId)) {
                    String itemName = poItemDetails[3];
                    int quantityToAdd = Integer.parseInt(poItemDetails[4]);

                    // update or add stock
                    boolean itemStockUpdated = false;

                    for (int j = 0; j < updatedStockLines.size(); j++) {
                        String[] stockDetails = updatedStockLines.get(j).split(",");
                        for (int k = 0; k < stockDetails.length; k++) {
                            stockDetails[k] = stockDetails[k].trim();
                        }

                        if (stockDetails[1].equalsIgnoreCase(itemName)) {
                            int currentStock = Integer.parseInt(stockDetails[2].trim());
                            stockDetails[2] = String.valueOf(currentStock + quantityToAdd);
                            updatedStockLines.set(j, String.join(",", stockDetails));
                            itemStockUpdated = true;
                            break;
                        }
                    }

                    // if item not found in stock, add a new entry
                    if (!itemStockUpdated) {
                        String newStockEntry = String.format(
                                "ST%03d,%s,%d,1,1000,In Stock,%s",
                                updatedStockLines.size() + 1,
                                itemName,
                                quantityToAdd,
                                new SimpleDateFormat("yyyy-MM-dd").format(new Date())
                        );
                        updatedStockLines.add(newStockEntry);
                    }
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

    public void generateStockReport(String savePath) throws IOException {
        Path stockFilePath = Paths.get(STOCK_DETAILS_FILE);

        if (!Files.exists(stockFilePath)) {
            throw new IOException("Stock file not found.");
        }

        // read the stock data
        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Stock ID,Name,Current Stock,Min Stock,Max Stock,Status,Date\n");
        for (String line : Files.readAllLines(stockFilePath)) {
            reportContent.append(line).append("\n");
        }

        // save the report to the specified path
        try (FileWriter writer = new FileWriter(savePath)) {
            writer.write(reportContent.toString());
        }
    }
}