package controller;

import java.util.List;

public class InventoryManagerController {
    private ItemController itemController;

    public InventoryManagerController(ItemController itemController) {
        this.itemController = itemController;
    }

    // laod items - View Items Button
    public Object[][] loadItems() {
        List<String> items = itemController.getAllItems();
        if (items == null || items.isEmpty()) {
            return null;
        }

        // convert item data to 2D Object array for the table
       return items.stream()
                .map(line -> line.split(","))
                .map(columns -> new Object[] {
                        columns[0], // item ID
                        columns[1], // item name
                        columns[4], // supplier
//                      columns[] // price
                        columns[2] // stock/quantity
                })
                .toArray(Object[][]::new);
    }

    // column names for table header
    public String[] getItemTableColumns() {
        return new String[]{"Item ID", "Item Name", "Supplier", "Stock"};
    }
}
