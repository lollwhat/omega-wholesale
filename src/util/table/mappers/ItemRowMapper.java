package util.table.mappers;

import util.table.RowDataMapper;

public class ItemRowMapper implements RowDataMapper {
    // Sample item data: IM001,Seeder Item A,1,kg,SUP001,2025-04-24 17:00:59,2025-04-24 17:00:59,sales1,sales1
    private static final int EXPECTED_DATA_FIELDS = 10;

    @Override
    public Object[] mapFieldsToRow(String[] fields, int expectedColumnCount) {
        if (fields.length < EXPECTED_DATA_FIELDS) {
            System.err.println("ItemMapper: Skipping malformed line (expected at least " +
                    EXPECTED_DATA_FIELDS + " fields, got " + fields.length + "): " + String.join(",", fields));
            return null;
        }

        Object[] rowData = new Object[expectedColumnCount];

        rowData[0] = fields[0];  // Item Entry ID
        rowData[1] = fields[1];  // Item Code
        rowData[2] = fields[2];  // Item Name
        rowData[3] = fields[3];  // Unit
        try { // Example of type conversion
            rowData[4] = Double.parseDouble(fields[4]);   // Price
        } catch (NumberFormatException e) {
            rowData[4] = fields[4];
            System.err.println("ItemMapper: Could not parse price for " + fields[0] + ". Value: " + fields[3]);
        }
        rowData[5] = fields[5];  // Supplier ID
        rowData[6] = fields[6];  // Created At
        rowData[7] = fields[8];  // Created By
        rowData[8] = fields[7];  // Updated At
        rowData[9] = fields[9];  // Updated By

        for (int i = EXPECTED_DATA_FIELDS; i < expectedColumnCount; i++) {
            rowData[i] = "";
        }
        return rowData;
    }
}
