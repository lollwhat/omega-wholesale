package view;

import controller.*;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SalesManagerView extends DashboardView {
    private final String[] quickOptions = {"Item Management", "Supplier Management", "Daily Sales Entry"};

    public SalesManagerView(SalesManager salesManager , String [] menuItems, String[] quickOptions) {
        super(salesManager, menuItems, quickOptions);
    }

    @Override
    protected JPanel createContentPanel(String contentType) {
        switch (contentType) {
            case "Item Management":
                ItemManagementView itemManagementPanel = new ItemManagementView();
                return itemManagementPanel.createItemManagementPanel();
            case "Supplier Management":
                SupplierManagementView supplierManagementView = new SupplierManagementView();
                return supplierManagementView.createSupplierManagementPanel();
            case "Stock Overview & Create Requisition":
                StockManagementView stockManagementView = new StockManagementView();
                return stockManagementView.createStockManagementPanel();
            case "Purchase Requisition Management":
                PurchaseRequisitionView purchaseRequisitionView = new PurchaseRequisitionView();
                return purchaseRequisitionView.createPurchaseRequisitionManagementPanel();
            case "List of Purchase Orders":
                PurchaseOrderView purchaseOrdersView = new PurchaseOrderView();
                return purchaseOrdersView.createPurchaseOrderPanel();
            case "Daily Sales Entry":
                DailySalesEntryView dailySalesEntryView = new DailySalesEntryView();
                return dailySalesEntryView.createDailySalesEntryPanel();
            default:
                return createDashboardContentPanel(quickOptions);
        }
    }
}