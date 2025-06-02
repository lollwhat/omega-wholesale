package view;

import controller.*;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminView extends DashboardView {

    public AdminView(Admin admin , String [] menuItems, String[] quickOptions) {
        super(admin, menuItems, quickOptions);
    }

    // "User Management", "Item Management", "Supplier Management", "Inventory Management", "Stock Overview & Create Requisition", "Purchase Requisition Management", "PR Overview and Create Order", "Receive Purchase Order", "Daily Sales Entry"
    @Override
    protected JPanel createContentPanel(String contentType) {
        switch (contentType){
            case "Dashboard":
                return createDashboardContentPanel(new String[]{"User Management", "Item Management", "Supplier Management"});
            case "User Management": //Admin
                UserManagementView userManagementPanel = new UserManagementView();
                return userManagementPanel.createUserManagementPanel();
            case "Item Management": //Sales Manager; Inventory Manager (View-Only)
                ItemManagementView itemManagementPanel = new ItemManagementView();
                return itemManagementPanel.createItemManagementPanel();
            case "Supplier Management": //Sales Manager; Inventory Manager (View-Only)
                SupplierManagementView supplierManagementView = new SupplierManagementView();
                return supplierManagementView.createSupplierManagementPanel();
            case "Inventory Management": // Inventory Manager
                IMStockManagementView imStockManagementView = new IMStockManagementView();
                return imStockManagementView.createStockManagementPanel();
            case "Stock Overview & Create Purchase Requisition": // Sales Manager
                StockManagementView stockManagementView = new StockManagementView();
                return stockManagementView.createStockManagementPanel();
            case "Purchase Requisition Management": // Sales Manager
                PurchaseRequisitionView purchaseRequisitionView = new PurchaseRequisitionView();
                return purchaseRequisitionView.createPurchaseRequisitionManagementPanel();
            case "PR Overview & Create Purchase Order": // Sales Manager
                PMPurchaseRequisitionView pmPurchaseRequisitionView = new PMPurchaseRequisitionView();
                return pmPurchaseRequisitionView.createPMPurchaseRequisitionPanel();
//            case "List of Purchase Orders": // Sales Manager; Purchase Manager
//                PurchaseOrderView purchaseOrdersView = new PurchaseOrderView();
//                return purchaseOrdersView.createPurchaseOrderPanel();
            case "Purchase Order Management": // Purchase Manager
                PMPurchaseOrderView pmPurchaseOrderView = new PMPurchaseOrderView();
                return pmPurchaseOrderView.createPurchaseOrderPanel();
            case "Receive Purchase Order": // Inventory Manager
                IMPurchaseOrderView purchaseOrderView = new IMPurchaseOrderView();
                return purchaseOrderView.createPurchaseOrderPanel();
            case "Daily Sales Entry": // Sales Manager; Inventory Manager (View-Only)
                DailySalesEntryView dailySalesEntryView = new DailySalesEntryView();
                return dailySalesEntryView.createDailySalesEntryPanel();
            default:
                return null;
        }
    }
}