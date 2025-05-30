package controller;

import model.EntityType;
import model.Stock;

public class SMStockController extends CRUDController<Stock> {
    public SMStockController() {
        super("data/stock_details.txt", EntityType.STOCK);
    }

    @Override
    public void add(Stock stock) {
        new FileController("data/stock_details.txt");
        try {
            String data = stock.toString();
            FileController.appendFile(data);
            System.out.println("Stock added successfully");
        } catch (Exception e) {
            System.out.println("Error adding stock to file: " + e.getMessage());
        }
    }

    @Override
    public void update(Stock stock) {
        new FileController("data/stock_details.txt");
        try {
            String data = stock.toString();
            fileController.updateFile(data);
            System.out.println("Stock updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating stock in file: " + e.getMessage());
        }
    }
}
