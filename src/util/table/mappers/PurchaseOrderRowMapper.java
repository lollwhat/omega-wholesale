package util.table.mappers;

import util.table.RowDataMapper;

public class PurchaseOrderRowMapper implements RowDataMapper {
    private static final int EXPECTED_HEADER_FIELDS = 10;
    // 0: pending, 1: approved, 2: received, 3: cancelled
    private static final String[] STATUS_MAP = {"Pending", "Approved", "Received", "Cancelled", "Unknown"};

    public PurchaseOrderRowMapper() {}

    public String getStatusString(int statusInt) {
        if (statusInt >= 0 && statusInt < STATUS_MAP.length -1) {
            return STATUS_MAP[statusInt];
        }
        return STATUS_MAP[STATUS_MAP.length -1] + " (" + statusInt + ")";
    }

    @Override
    public Object[] mapFieldsToRow(String[] headerFields, int expectedDisplayColumnCount) {
        if (headerFields.length < EXPECTED_HEADER_FIELDS) {
            System.err.println("PurchaseOrderRowMapper: Skipping malformed PO header line (expected " +
                    EXPECTED_HEADER_FIELDS + " fields, got " + headerFields.length + "): " + String.join(",", headerFields));
            return null;
        }

        Object[] rowData = new Object[expectedDisplayColumnCount];

        int currentDataIndex = 0;
        int currentRowDataIndex = 0;

        rowData[currentRowDataIndex++] = headerFields[currentDataIndex++];
        rowData[currentRowDataIndex++] = headerFields[currentDataIndex++].replace(";", ",");
        rowData[currentRowDataIndex++] = headerFields[currentDataIndex++].replace(";", ",");

        try {
            int statusInt = Integer.parseInt(headerFields[currentDataIndex++]);
            rowData[currentRowDataIndex++] = getStatusString(statusInt);
        } catch (NumberFormatException e) {
            rowData[currentRowDataIndex++] = "Invalid Status";
            System.err.println("PurchaseOrderRowMapper: Could not parse status for " + headerFields[0] + ". Value: " + headerFields[currentDataIndex-1]);
        }
        rowData[currentRowDataIndex++] = headerFields[currentDataIndex++];
        rowData[currentRowDataIndex++] = headerFields[currentDataIndex++];
        rowData[currentRowDataIndex++] = headerFields[currentDataIndex++];
        rowData[currentRowDataIndex++] = headerFields[currentDataIndex++];

        String receivedAt = headerFields[currentDataIndex++];
        rowData[currentRowDataIndex++] = (receivedAt == null || receivedAt.isEmpty() || receivedAt.equalsIgnoreCase("null")) ? "N/A" : receivedAt;

        String receivedBy = headerFields[currentDataIndex++];
        rowData[currentRowDataIndex++] = (receivedBy == null || receivedBy.isEmpty() || receivedBy.equalsIgnoreCase("null")) ? "N/A" : receivedBy;


        for (int i = currentRowDataIndex; i < expectedDisplayColumnCount; i++) {
            rowData[i] = "";
        }
        return rowData;
    }
}