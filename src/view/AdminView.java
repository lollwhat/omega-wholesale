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


    @Override
    protected JPanel createContentPanel(String contentType) {
        switch (contentType){
            case "Dashboard":
                return createDashboardContentPanel(new String[]{"User Management", "Item Management", "Supplier Management"});
            case "User Management":
                UserManagementView userManagementPanel = new UserManagementView();
                return userManagementPanel.createUserManagementPanel();
//            case "Item Management":
//                return createItemManagementPanel();
//            case "Supplier Management":
//                return createSupplierManagementPanel();
//            case "Daily Sales Entry":
//                return createDailySalesEntryPanel();
//            case "Create Purchase Requisition":
//                return createPurchaseRequisitionPanel();
//            case "View Requisitions":
//                return createViewRequisitionsPanel();
//            case "Create Purchase Order":
//                return createPurchaseOrderPanel();
//            case "View Purchase Orders":
//                return createViewPurchaseOrdersPanel();
            default:
                return null;
        }
    }
}