package view;

import model.InventoryManager;

import javax.swing.*;

public class InventoryManagerView extends DashboardView {
    private final String[] quickOptions = {"View Item", "Inventory Management", "Receive Purchase Order"};

    public InventoryManagerView(InventoryManager inventoryManager , String [] menuItems, String[] quickOptions) {
        super(inventoryManager, menuItems, quickOptions);
    }

    @Override
    protected JPanel createContentPanel(String contentType) {
        switch (contentType) {
            case "View Item":
                ItemManagementView itemManagementPanel = new ItemManagementView();
                return itemManagementPanel.createViewItemPanel();
            case "Inventory Management":
                IMStockManagementView stockManagementView = new IMStockManagementView();
                return stockManagementView.createStockManagementPanel();
            case "Receive Purchase Order":
                IMPurchaseOrderView purchaseOrderView = new IMPurchaseOrderView();
                return purchaseOrderView.createPurchaseOrderPanel();
            default:
                return createDashboardContentPanel(quickOptions);
        }
    }
}