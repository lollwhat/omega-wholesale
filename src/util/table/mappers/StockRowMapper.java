package util.table.mappers;

import util.table.RowDataMapper;

public class StockRowMapper implements RowDataMapper {
    // Expected format from stock_details.txt based on Stock.java toString():
    // StockID,ItemCode,ItemName,CurrentStock,MinStock,MaxStock,Status,LastUpdateDate
    private static final int EXPECTED_STOCK_DETAIL_FIELDS = 8;

    @Override
    public Object[] mapFieldsToRow(String[] fields, int expectedColumnCount) {
        if (fields.length < EXPECTED_STOCK_DETAIL_FIELDS) {
            System.err.println("StockRowMapper: Skipping malformed line (expected at least " +
                    EXPECTED_STOCK_DETAIL_FIELDS + " fields, got " + fields.length + "): " + String.join(",", fields));
            return null;
        }

        Object[] rowData = new Object[expectedColumnCount];

        // Assuming column order in StockManagementView:
        // "Stock ID", "Item Code", "Item Name", "Current Stock", "Min Stock", "Max Stock", "Status", "Last Update Date" (, "Actions")

        rowData[0] = fields[0]; // Stock ID (e.g., ST001)
        rowData[1] = fields[1]; // Item Code (e.g., LAP001)
        rowData[2] = fields[2]; // Item Name (e.g., HP EliteBook G10)
        try {
            rowData[3] = Integer.parseInt(fields[3]); // Current Stock
        } catch (NumberFormatException e) {
            rowData[3] = fields[3]; // Fallback
            System.err.println("StockRowMapper: Could not parse Current Stock for " + fields[0] + ". Value: " + fields[3]);
        }
        try {
            rowData[4] = Integer.parseInt(fields[4]); // Min Stock
        } catch (NumberFormatException e) {
            rowData[4] = fields[4]; // Fallback
            System.err.println("StockRowMapper: Could not parse Min Stock for " + fields[0] + ". Value: " + fields[4]);
        }
        try {
            rowData[5] = Integer.parseInt(fields[5]); // Max Stock
        } catch (NumberFormatException e) {
            rowData[5] = fields[5]; // Fallback
            System.err.println("StockRowMapper: Could not parse Max Stock for " + fields[0] + ". Value: " + fields[5]);
        }
        rowData[6] = fields[6]; // Status
        rowData[7] = fields[7]; // Last Update Date

        // Fill any remaining columns (e.g., "Actions" if present)
        for (int i = EXPECTED_STOCK_DETAIL_FIELDS; i < expectedColumnCount; i++) {
            rowData[i] = "";
        }
        return rowData;
    }
}