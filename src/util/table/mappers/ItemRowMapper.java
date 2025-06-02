package util.table.mappers;

import controller.ItemSupplierController;
import util.table.RowDataMapper;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRowMapper implements RowDataMapper {
    // Sample item data: IM001,Seeder Item A,1,kg,SUP001,2025-04-24 17:00:59,2025-04-24 17:00:59,sales1,sales1
    private static final int EXPECTED_ITEM_DETAIL_FIELDS = 9;
    private final ItemSupplierController itemSupplierController;

    public ItemRowMapper(ItemSupplierController itemSupplierController) {
        this.itemSupplierController = itemSupplierController;
    }

    @Override
    public Object[] mapFieldsToRow(String[] fields, int expectedColumnCount) {
        if (fields.length < EXPECTED_ITEM_DETAIL_FIELDS) { // Check against 9
            System.err.println("ItemRowMapper: Skipping malformed line from item_details.txt (expected at least " +
                    EXPECTED_ITEM_DETAIL_FIELDS + " fields, got " + fields.length + "): " + String.join(",", fields));
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
        String itemId = fields[0];
        if (this.itemSupplierController != null) {
            List<String> supplierIds = this.itemSupplierController.getSupplierIdsForItem(itemId);
            if (supplierIds != null && !supplierIds.isEmpty()) {
                rowData[5] = supplierIds.stream().collect(Collectors.joining(", "));
            } else {
                rowData[5] = "N/A";
            }
        } else {
            rowData[5] = "Error: ISC not init";
            System.err.println("ItemRowMapper: ItemSupplierController is null for item " + itemId);
        }
        rowData[6] = fields[5];  // Created At
        rowData[7] = fields[7];  // Created By
        rowData[8] = fields[6];  // Updated At
        rowData[9] = fields[8];  // Updated By

        if (expectedColumnCount > 10) {
            for (int i = 10; i < expectedColumnCount; i++) {
                rowData[i] = "";
            }
        }
        return rowData;
    }
}
