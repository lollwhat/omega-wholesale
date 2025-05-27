package util.table.mappers;

import util.table.RowDataMapper;
import controller.SupplierController;

import java.util.HashMap;
import java.util.Map;

public class PurchaseRequisitionRowMapper implements RowDataMapper {
    // PR Header CSV: prId,notes,supplierId,status,createdAt,createdBy,updatedAt,updatedBy
    private static final int EXPECTED_HEADER_FIELDS = 8;
    private static final String[] STATUS_MAP = {"Pending", "Approved", "Rejected", "Cancelled"};

    private Map<String, String> supplierNameCache = new HashMap<>();

    public PurchaseRequisitionRowMapper() {
    }

    private String getSupplierDisplayName(String supplierId) {
        SupplierController supplierController = new SupplierController();
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return "N/A";
        }
        if (supplierNameCache.containsKey(supplierId)) {
            return supplierNameCache.get(supplierId);
        }
        if (supplierController == null) {
            supplierController = new SupplierController();
        }

        String supplierData = supplierController.getOneWithId(supplierId);
        if (supplierData != null) {
            String[] parts = supplierData.split(",");
            if (parts.length > 1) {
                String displayName = parts[0] + " - " + parts[1];
                supplierNameCache.put(supplierId, displayName);
                return displayName;
            }
        }
        supplierNameCache.put(supplierId, supplierId + " (Not Found)");
        return supplierId + " (Not Found)";
    }


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
        rowData[0] = headerFields[0]; // PR ID
        rowData[1] = headerFields[1].replace(";", ","); // Notes (unescape semicolon if used)
        rowData[2] = getSupplierDisplayName(headerFields[2]); // Supplier ID -> Display Name (e.g., "SUP001 - Acme Corp")
        try {
            int statusInt = Integer.parseInt(headerFields[3]);
            rowData[3] = getStatusString(statusInt); // Status
        } catch (NumberFormatException e) {
            rowData[3] = "Invalid Status";
            System.err.println("PurchaseRequisitionRowMapper: Could not parse status for " + headerFields[0] + ". Value: " + headerFields[3]);
        }
        rowData[4] = headerFields[4]; // Created At
        rowData[5] = headerFields[5]; // Created By
        rowData[6] = headerFields[6]; // Updated At
        rowData[7] = headerFields[7]; // Updated By

        if (expectedDisplayColumnCount > EXPECTED_HEADER_FIELDS) {
            for (int i = EXPECTED_HEADER_FIELDS; i < expectedDisplayColumnCount; i++) {
                rowData[i] = "";
            }
        }
        return rowData;
    }
}
