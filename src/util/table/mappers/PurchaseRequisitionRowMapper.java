package util.table.mappers;

import util.table.RowDataMapper;
// No longer needs SupplierController as there's no header-level supplier ID to resolve
// import controller.SupplierController;
// import java.util.HashMap;
// import java.util.Map;

public class PurchaseRequisitionRowMapper implements RowDataMapper {
    // Updated PR Header CSV (from PurchaseRequisition.java):
    // prId,notes,status,createdAt,createdBy,updatedAt,updatedBy
    private static final int EXPECTED_HEADER_FIELDS = 7; // Was 8
    private static final String[] STATUS_MAP = {"Pending", "Approved", "Rejected", "Cancelled", "PO Created"};

    // No longer need supplierNameCache or SupplierController instance here
    // private Map<String, String> supplierNameCache = new HashMap<>();

    public PurchaseRequisitionRowMapper() {
        // Constructor is now simpler
    }

    // getSupplierDisplayName method is no longer needed as there's no header supplier.

    public String getStatusString(int statusInt) {
        if (statusInt >= 0 && statusInt < STATUS_MAP.length) {
            return STATUS_MAP[statusInt];
        }
        return "Unknown (" + statusInt + ")";
    }

    @Override
    public Object[] mapFieldsToRow(String[] headerFields, int expectedDisplayColumnCount) {
        if (headerFields.length < EXPECTED_HEADER_FIELDS) {
            System.err.println("PurchaseRequisitionRowMapper: Skipping malformed PR header line (expected " +
                    EXPECTED_HEADER_FIELDS + " fields, got " + headerFields.length + "): " + String.join(",", headerFields));
            return null;
        }

        Object[] rowData = new Object[expectedDisplayColumnCount];
        // Assuming PurchaseRequisitionView.prColumnNames will be updated:
        // Old: "PR ID", "Notes", "Supplier", "Status", "Created At", "Created By", "Updated At", "Updated By", "Actions"
        // New: "PR ID", "Notes", "Status", "Created At", "Created By", "Updated At", "Updated By", "Actions"
        // (Supplier column removed from view's main table, or shown as "Per Item")

        int colIdx = 0;
        rowData[colIdx++] = headerFields[0]; // PR ID (prId)
        rowData[colIdx++] = headerFields[1].replace(";", ","); // Notes (notes)

        // headerFields[2] was supplierId, now it's status
        try {
            int statusInt = Integer.parseInt(headerFields[2].trim()); // Status (status)
            rowData[colIdx++] = getStatusString(statusInt);
        } catch (NumberFormatException e) {
            rowData[colIdx++] = "Invalid Status";
            System.err.println("PurchaseRequisitionRowMapper: Could not parse status for " + headerFields[0] + ". Value: " + headerFields[2]);
        }

        rowData[colIdx++] = headerFields[3]; // Created At (createdAt - was headerFields[4])
        rowData[colIdx++] = headerFields[4]; // Created By (createdBy - was headerFields[5])
        rowData[colIdx++] = headerFields[5]; // Updated At (updatedAt - was headerFields[6])
        rowData[colIdx++] = headerFields[6]; // Updated By (updatedBy - was headerFields[7])

        // Fill remaining columns (e.g., "Actions" column)
        for (int i = colIdx; i < expectedDisplayColumnCount; i++) {
            rowData[i] = "";
        }
        return rowData;
    }
}