package util.table.mappers;

import util.table.RowDataMapper;

public class SupplierRowMapper implements RowDataMapper {
    // Sample supplier data: SUP001,Seeder Supplier A,Seeder PIC A,Seeder Description A,Seeder Address A,08123456789,2025-04-24 17:09:04,2025-04-24 17:09:04,sales1,sales1
    private static final int EXPECTED_DATA_FIELDS = 12;

    @Override
    public Object[] mapFieldsToRow(String[] fields, int expectedColumnCount) {
        if (fields.length < EXPECTED_DATA_FIELDS) {
            System.err.println("SupplierMapper: Skipping malformed line (expected at least " +
                    EXPECTED_DATA_FIELDS + " fields, got " + fields.length + "): " + String.join(",", fields));
            return null;
        }

        Object[] rowData = new Object[expectedColumnCount];

        rowData[0] = fields[0];  // Supplier ID
        rowData[1] = fields[1];  // Supplier Company
        rowData[2] = fields[2];  // PIC
        rowData[3] = fields[3];  // Description
        rowData[4] = fields[4];  // Address
        rowData[5] = fields[5];  // Phone
        rowData[6] = fields[6];  // Email
        switch (fields[7]) {
            case "0":
                rowData[7] = "Pending";
                break;
            case "1":
                rowData[7] = "Approved";
                break;
            case "2":
                rowData[7] = "Suspended";
                break;
            default:
                rowData[7] = "Unknown";
        }
        rowData[8] = fields[8];  // Created At
        rowData[9] = fields[10];  // Created By
        rowData[10] = fields[9];  // Updated At
        rowData[11] = fields[11];  // Updated By

        for (int i = EXPECTED_DATA_FIELDS; i < expectedColumnCount; i++) {
            rowData[i] = "";
        }
        return rowData;
    }
}
