package controller;

import model.EntityType;
import model.DailySalesEntry;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailySalesEntryController extends CRUDController<DailySalesEntry> {
    public DailySalesEntryController() {
        super("data/daily_sales_entry.txt", EntityType.DAILY_SALES_ENTRY);
    }

    @Override
    public void add(DailySalesEntry dailySalesEntry) {
        try {
            String data = dailySalesEntry.toCSV();
            FileController.appendFile(data);
            System.out.println("Daily sales entered successfully");
        } catch (Exception e) {
            System.out.println("Error entering daily sales to file: " + e.getMessage());
        }
    }

    public void addDailySalesEntry(String itemId, String itemName, String salesDate, int quantity, double netIncome) {
        int tempItemId = FileController.getFile().size()+1;
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String createdBy = SessionController.getInstance().getUserId();
        String updatedBy = SessionController.getInstance().getUserId();
        DailySalesEntry dailySalesEntry = new DailySalesEntry("DSE" + String.format("%03d", tempItemId), itemId, itemName, salesDate, quantity, netIncome, createdAt, createdBy, createdAt, updatedBy);
        add(dailySalesEntry);
        String data = dailySalesEntry.toCSV();
        FileController.appendFile(data);
        System.out.println("Daily sales entered successfully");
    }

    @Override
    public void update(DailySalesEntry dailySalesEntry) {
        try {
            String data = dailySalesEntry.toCSV();
            fileController.updateFile(data);
            System.out.println("Daily sales entry updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating daily sales entry in file: " + e.getMessage());
        }
    }

    public void updateDailySalesEntry(String dailySalesId, String itemId, String itemName, String salesDate, int quantity, double netIncome) {
        try {
            String[] existingDailySalesEntryDetails = fileController.getLine(0, dailySalesId);
            if (existingDailySalesEntryDetails == null) {
                System.out.println("Daily Sales Entry not found");
                return;
            }
            DailySalesEntry dailySalesEntry = new DailySalesEntry(dailySalesId, itemId, itemName, salesDate, quantity, netIncome, existingDailySalesEntryDetails[4], existingDailySalesEntryDetails[5], existingDailySalesEntryDetails[6], existingDailySalesEntryDetails[7]);
            dailySalesEntry.setItemId((itemId != null) ? itemId : existingDailySalesEntryDetails[1]);
            dailySalesEntry.setItemName((itemName != null) ? itemName : existingDailySalesEntryDetails[2]);
            dailySalesEntry.setSalesDate((salesDate != null) ? salesDate : existingDailySalesEntryDetails[3]);
            dailySalesEntry.setQuantity((quantity != 0) ? quantity : Integer.parseInt(existingDailySalesEntryDetails[4]));
            dailySalesEntry.setNetIncome((netIncome != 0) ? netIncome : Double.parseDouble(existingDailySalesEntryDetails[5]));
            dailySalesEntry.setCreatedAt(existingDailySalesEntryDetails[6]);
            dailySalesEntry.setCreatedBy(existingDailySalesEntryDetails[7]);
            dailySalesEntry.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            dailySalesEntry.setUpdatedBy(SessionController.getInstance().getUserId());
            update(dailySalesEntry);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
