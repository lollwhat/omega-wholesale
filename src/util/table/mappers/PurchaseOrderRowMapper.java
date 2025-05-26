package util.table.mappers;

import util.table.RowDataMapper;
import controller.SupplierController;

import java.util.HashMap;
import java.util.Map;

public class PurchaseOrderRowMapper implements RowDataMapper {
    private static final int EXPECTED_HEADER_FIELDS = 11;
    // 0: processing, 1: received, 2: cancelled
    private static final String[] STATUS_MAP = {"Processing", "Received", "Cancelled", "Unknown"};

    private Map<String, String> supplierNameCache;
    private SupplierController supplierController;

    public PurchaseOrderRowMapper(SupplierController supplierController) {
        this.supplierController = supplierController;
        this.supplierNameCache = new HashMap<>();
    }

    private String getSupplierDisplayName(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty() || supplierId.equalsIgnoreCase("null")) {
            return "N/A";
        }
        if (supplierNameCache.containsKey(supplierId)) {
            return supplierNameCache.get(supplierId);
        }

        if (this.supplierController == null) {
            System.err.println("PurchaseOrderRowMapper: SupplierController not initialized. Cannot fetch supplier name.");
            supplierNameCache.put(supplierId, supplierId + " (Controller N/A)");
            return supplierId + " (Controller N/A)";
        }

        new controller.FileController("data/supplier_details.txt");

        String supplierData = supplierController.getOneWithId(supplierId);
        if (supplierData != null) {
            String[] parts = supplierData.split(",");
            if (parts.length > 1) {
                String displayName = parts[0].trim() + " - " + parts[1].trim().replace(";", ",");
                supplierNameCache.put(supplierId, displayName);
                return displayName;
            } else {
                supplierNameCache.put(supplierId, parts[0].trim() + " (Incomplete Data)");
                return parts[0].trim() + " (Incomplete Data)";
            }
        }
        supplierNameCache.put(supplierId, supplierId + " (Not Found)");
        return supplierId + " (Not Found)";
    }

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

        rowData[0] = headerFields[0]; // poId
        rowData[1] = headerFields[1].replace(";", ","); // prId
        rowData[2] = headerFields[2].replace(";", ","); // notes
        rowData[3] = getSupplierDisplayName(headerFields[3]); // supplierId -> Display Name
        try {
            int statusInt = Integer.parseInt(headerFields[4]);
            rowData[4] = getStatusString(statusInt); // status (int to String)
        } catch (NumberFormatException e) {
            rowData[4] = "Invalid Status";
            System.err.println("PurchaseOrderRowMapper: Could not parse status for " + headerFields[0] + ". Value: " + headerFields[4]);
        }
        rowData[5] = headerFields[5];  // createdAt
        rowData[6] = headerFields[6];  // createdBy
        rowData[7] = headerFields[7];  // updatedAt
        rowData[8] = headerFields[8];  // updatedBy
        rowData[9] = (headerFields[9] == null || headerFields[9].isEmpty() || headerFields[9].equalsIgnoreCase("null")) ? "N/A" : headerFields[9]; // receivedAt
        rowData[10] = (headerFields[10] == null || headerFields[10].isEmpty() || headerFields[10].equalsIgnoreCase("null")) ? "N/A" : headerFields[10]; // receivedBy

        if (expectedDisplayColumnCount > EXPECTED_HEADER_FIELDS) {
            for (int i = EXPECTED_HEADER_FIELDS; i < expectedDisplayColumnCount; i++) {
                rowData[i] = "";
            }
        }
        return rowData;
    }
}