package util.table.mappers;

import util.table.RowDataMapper;

public class DailySalesRowMapper implements RowDataMapper {
    // Sample daily sales entry data: DSE005,Seeder Item ID E,Seeder Item Name E,Seeder Sales Date E,60,180,2025-05-23 14:10:34,sales1,2025-05-23 14:10:34,sales1
    private static final int EXPECTED_DATA_FIELDS = 10; // Number of fields from the data string

    @Override
    public Object[] mapFieldsToRow(String[] fields, int expectedColumnCount) {
        if (fields.length < EXPECTED_DATA_FIELDS) {
            System.err.println("DailySalesMapper: Skipping malformed line (expected at least " +
                    EXPECTED_DATA_FIELDS + " fields, got " + fields.length + "): " + String.join(",", fields));
            return null; // Indicate row should be skipped
        }

        Object[] rowData = new Object[expectedColumnCount];

        // Map data fields to the beginning of rowData
        rowData[0] = fields[0];  // Sales Entry ID
        rowData[1] = fields[1];  // Item ID
        rowData[2] = fields[2];  // Item Name
        rowData[3] = fields[3];  // Sales Date
        try { // Example of type conversion
            rowData[4] = Integer.parseInt(fields[4]);   // unit
        } catch (NumberFormatException e) {
            rowData[4] = fields[4]; // Fallback to string or handle error
            System.err.println("DailySalesMapper: Could not parse unit for " + fields[0] + ". Value: " + fields[3]);
        }
        try {
            rowData[5] = Double.parseDouble(fields[5]);   // Net Income
        } catch (NumberFormatException e) {
            rowData[5] = fields[5]; // Fallback to string or handle error
            System.err.println("DailySalesMapper: Could not parse net income for " + fields[0] + ". Value: " + fields[3]);
        }
        rowData[6] = fields[6];  // Created At
        rowData[7] = fields[7];  // Created By
        rowData[8] = fields[8];  // Updated At
        rowData[9] = fields[9];  // Updated By

        // Fill remaining columns (e.g., "Actions" column) if expectedColumnCount is larger
        for (int i = EXPECTED_DATA_FIELDS; i < expectedColumnCount; i++) {
            rowData[i] = ""; // Default for additional columns like "Actions"
        }
        return rowData;
    }
}
